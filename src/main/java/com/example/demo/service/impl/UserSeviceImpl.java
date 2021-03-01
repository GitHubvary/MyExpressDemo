package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.constant.RedisKeyConstant;
import com.example.demo.common.util.CollectionUtils;
import com.example.demo.common.util.StringUtils;
import com.example.demo.domain.bean.UserEvaluate;
import com.example.demo.domain.enums.UserRoleEnum;
import com.example.demo.domain.vo.LayuiTableVO;
import com.example.demo.domain.vo.admin.AdminUserInfoVO;
import com.example.demo.domain.vo.user.UserInfoVO;
import com.example.demo.mapper.UserMapper;
import com.example.demo.domain.enums.ResponseErrorCodeEnum;
import com.example.demo.domain.ResponseResult;
import com.example.demo.domain.bean.User;
import com.example.demo.service.UserEvaluateService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Description:实现用户数据库操作的实现类
 * date: 2021/1/16 16:00
 */
@Service
@Transactional
public class UserSeviceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    //声明mapper属性
    @Autowired(required = false)
    private  UserMapper userMapper;

    @Autowired
    private DataSourceTransactionManager transactionManager;

    @Autowired
    private UserEvaluateService userEvaluateService;

    @Resource
    private RedisTemplate<String, User> redisTemplate;

    /**
     * Description:查询用户信息，校验输入登录用户信息是否存在且正确
     * @param:
     * @return:
    */
    @Override
    public User getByInfo(String username,String password) {
        List<User> userList = userMapper.selectList(new QueryWrapper<User>().eq("username", username));
        User user = CollectionUtils.getListFirst(userList);
        if(user ==null){
            return  null;
        }
        if (user.getPassword().equals(password)) {         //得用equals(),不要用==,值类型不同,排查了我大半天。1月16日19点00分
            return user;
        }
        return null;

    }

    /**
     * Description:查询是否存在同名用户
     * @param:
     * @return:
    */
    @Override
    public User getByName(String username) {
        List<User> userList = userMapper.selectList(new QueryWrapper<User>().eq("username", username));
        User user = CollectionUtils.getListFirst(userList);
        if(user ==null){
            return  null;
        }
        return user;
    }

    /**
     * Description:注册页面数据库插入用户信息
    */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)  //开启新事务
    public ResponseResult registerByName(String username, String password) {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(definition);

        if(checkExistByUsername(username)) {
            transactionManager.rollback(status);
            System.out.println(ResponseResult.failure(ResponseErrorCodeEnum.USERNAME_EXIST));
            return ResponseResult.failure(ResponseErrorCodeEnum.USERNAME_EXIST);
        }

        User user = User.builder()
                    .username(username)
                    .password(password)
                    .role(UserRoleEnum.DIS_FORMAL)
                    .build();

        if(!this.retBool(userMapper.insert(user))) {
            transactionManager.rollback(status);
            return ResponseResult.failure(ResponseErrorCodeEnum.REGISTRY_ERROR);
        }

        transactionManager.commit(status);
        System.out.println(ResponseResult.success());
        return ResponseResult.success();
    }

    /**
     * Description:查询用户名是否已存在
     * @param:
     * @return:
    */
    @Override
    public boolean checkExistByUsername(String username) {
        String name = username.trim();
        return getByName(name) != null;
    }

    /**
     * Description:修改密码
     * @param:
     * @return:
    */
    @Override
    public ResponseResult resetPassword(String userId, String oldPassword, String newPassword) {
        User user = getById(userId);

        if(!oldPassword.equals(user.getPassword())) {
            System.out.println("原密码输入错误,修改失败");
            return ResponseResult.failure(ResponseErrorCodeEnum.PASSWORD_ERROR);
        }

        user.setPassword(newPassword);

        if(!updateById(user)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PASSWORD_RESET_ERROR);
        }

        System.out.println("密码修改成功！");
        return ResponseResult.success();
    }

    @Override
    public UserInfoVO getUserInfo(String userId) {
        User user = getById(userId);
        UserRoleEnum userRole = user.getRole();

        UserInfoVO vo = UserInfoVO.builder()
                        .username(user.getUsername())
                        .idCard(user.getIdCard())
                        .realName(user.getRealName())
                        .tel(user.getTel())
                        .role(String.valueOf(userRole.getType()))
                        .roleName(userRole.getCnName())
                        .school(user.getSchoolName())
                        .account(user.getAccount())
                        .studentId(user.getStudentId())
                        .build();

        return vo;
    }

    @Override
    public Map<String, Integer> getAdminDashboardData() {
        Map<String, Integer> map = new HashMap<>();

        Integer todayCount = userMapper.selectCount(new QueryWrapper<User>().between("create_date",
                LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT), LocalDateTime.now()));

        Integer totalCount = userMapper.selectCount(null);

        Integer disEnableCount = userMapper.selectCount(new QueryWrapper<User>().eq("has_enable", 0));

        Integer lockCount = userMapper.selectCount(new QueryWrapper<User>().gt("lock_date", LocalDateTime.now()));

        map.put("today", todayCount);
        map.put("total", totalCount);
        map.put("disEnable", disEnableCount);
        map.put("lock", lockCount);

        return map;
    }

    @Override
    public LayuiTableVO<AdminUserInfoVO> pageAdminUserInfoVO(Page<User> page, QueryWrapper<User> wrapper) {
        IPage<User> selectPage = userMapper.selectPage(page, wrapper);
        LayuiTableVO<AdminUserInfoVO> result = new LayuiTableVO<>();

        result.setTotal(selectPage.getTotal());
        result.setRows(convert(selectPage.getRecords()));

        return result;
    }

    @Override
    public String getFrontName(String userId) {
        return getFrontName(getById(userId));
    }

    @Override
    public String getFrontName(User user) {

        String username = user.getUsername();

        return username;
    }

    @Override
    public User getById(Serializable id) {
        User user = (User) redisTemplate.opsForHash().get(RedisKeyConstant.SYS_USER, id);
        if(user != null) {
            return user;
        }
        user = super.getById(id);

        redisTemplate.opsForHash().put(RedisKeyConstant.SYS_USER, id, user);
        return user;
    }

    private List<AdminUserInfoVO> convert(List<User> users) {
        if(CollectionUtils.isListEmpty(users)) {
            return Collections.emptyList();
        }

        return users.stream().map(this::convert).collect(Collectors.toList());
    }

    private AdminUserInfoVO convert(User user) {
        AdminUserInfoVO vo = AdminUserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .tel(user.getTel())
                .role(user.getRole().getType())
                .hasReal(!StringUtils.isAnyBlank(user.getRealName(), user.getIdCard()))
                .hasEnable(user.getHasEnable() == 1)
                .createDate(user.getCreateDate()).build();

        LocalDateTime lockDate = user.getLockDate();
        if(lockDate != null && LocalDateTime.now().isBefore(lockDate)) {
            vo.setLockDate(lockDate);
        }

        UserEvaluate userEvaluate = userEvaluateService.getById(user.getId());
        if(userEvaluate != null && userEvaluate.getCount() > 0) {
            vo.setScore(userEvaluate.getScore().toPlainString());
        }

        return vo;
    }


}
