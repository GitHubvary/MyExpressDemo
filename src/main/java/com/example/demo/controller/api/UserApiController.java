package com.example.demo.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.util.StringUtils;
import com.example.demo.domain.ResponseResult;
import com.example.demo.domain.bean.User;
import com.example.demo.domain.enums.ResponseErrorCodeEnum;
import com.example.demo.domain.enums.UserRoleEnum;
import com.example.demo.domain.vo.LayuiTableVO;
import com.example.demo.domain.vo.admin.AdminUserInfoVO;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:用户信息 API Controller
 * date: 2021/2/19 18:10
 */
@RestController
@RequestMapping("/user")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserApiController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取用户列表
     * @param id 用户ID
     * @param isReal 是否实名认证
     * @param isEnable 是否启用
     * @param isLock 是否冻结
     * @param username 用户名
     * @param tel 手机号
     */
    @RequestMapping("/list")
    /*
      前台表格开启分页后，并不代表着可以进行分页了，需要和后台进行交互，
      开启分页只是默认给后台的方法传入两个参数（page和limit）
      后台接收前台的分页参数，注意，这里的page和limit参数并没有显性的展示出来，
      只是表格开启分页之后默认就传给后台了，参数名是固定的。
      */
    public LayuiTableVO<AdminUserInfoVO> listUser(@RequestParam Integer page, //当前页
                                                  @RequestParam Integer limit,  //每页显示行数
                                                  String isReal, String isEnable, String isLock, String role,
                                                  String id, String username, String tel) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        System.out.println("用户列表当前页："+page+"\t每页显示行数："+limit);
        System.out.println("isReal："+isReal+'\t'+"isEnable："+isEnable+'\t'+"isLock："+isLock+'\t'+"role："+role+'\t'+"id："+id+'\t'+"username："+username+'\t'+"tel："+tel);
        Integer enable = StringUtils.toInteger(isEnable, -1);
        if (enable != -1) {
            wrapper.eq("has_enable", enable);
        }

        Integer roleType = StringUtils.toInteger(role, -1);
        UserRoleEnum roleEnum = UserRoleEnum.getByType(roleType);
        if (roleEnum != null) {
            wrapper.eq("role_id", roleType);
        }

        Integer userId = StringUtils.toInteger(id);

        if (StringUtils.isNotBlank(id)) {
            wrapper.eq("uid", userId);
        }

        if (StringUtils.isNotBlank(username)) {
            wrapper.like("username", username);
        }

        if (StringUtils.isNotBlank(tel)) {
            wrapper.eq("tel", tel);
        }

        Integer lock = StringUtils.toInteger(isLock, -1);
        if (lock == 1) {
            wrapper.ge("lock_date", LocalDateTime.now());
        } else if (lock == 0) {
            wrapper.isNull("lock_date");
        }

        Integer real = StringUtils.toInteger(isReal, -1);
        if (real == 1) {
            wrapper.isNotNull("real_name").isNotNull("id_card");
        } else if (real == 0) {
            wrapper.isNull("real_name").isNull("id_card");
        }

        System.out.println(userService.pageAdminUserInfoVO(new Page<>(page, limit), wrapper));

        return userService.pageAdminUserInfoVO(new Page<>(page, limit), wrapper);
    }

    /**
     * 改变用户状态
     * @param type 1. 禁用；2：启用；3：冻结；4：解冻
     * @param hour 冻结小时数
     */
    @PostMapping("/{id}/status")
    public ResponseResult changeStatus(@PathVariable String id, String type, String hour) {
        Integer op = StringUtils.toInteger(type, -1);
        User user = userMapper.selectById(id);
        System.out.println("更新前"+user.getHasEnable());
        if(user.getRole() == UserRoleEnum.ADMIN) {
            return ResponseResult.failure(ResponseErrorCodeEnum.NO_PERMISSION);
        }

        switch (op) {
            case 1:
                user.setHasEnable(0);
                break;
            case 2:
                user.setHasEnable(1);
                System.out.println("启用后"+user.getHasEnable());
                break;
            case 3:
                // 必须为正整数
                Integer lockHour = StringUtils.toInteger(hour, -1);
                if(lockHour == -1 || lockHour < 0) {
                    return ResponseResult.failure(ResponseErrorCodeEnum.MUST_POSITIVE_INTEGER);
                }
                user.setLockDate(LocalDateTime.now().plusHours(lockHour));
                break;
            case 4:
                user.setLockDate(LocalDateTime.now());
                break;
            default:
                return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }

//        userMapper.updateById(user);

        if(userService.updateById(user)) {
            System.out.println("更新后"+user.getHasEnable());
            return ResponseResult.success();
        } else {
            System.out.println("更新后"+user.getHasEnable());
            return ResponseResult.failure(ResponseErrorCodeEnum.OPERATION_ERROR);
        }
    }

    /**
     * Description:加载配送员列表
    */
    @GetMapping("/courier-list")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseResult listCourier() {
        List<User> users = userService.list(new QueryWrapper<User>().eq("role_id", UserRoleEnum.COURIER.getType()));
        if(users.size() == 0) {
            return ResponseResult.success();
        }

        List<Map> result = new ArrayList<>();
        for(User user : users) {
            Map<String ,String> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("name", userService.getFrontName(user));
            map.put("tel",user.getTel());
            result.add(map);
        }

        System.out.println("加载快递员列表："+result);

        return ResponseResult.success(result);
    }


}
