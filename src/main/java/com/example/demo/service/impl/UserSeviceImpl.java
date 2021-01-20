package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.CollectionUtils;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.ResponseErrorCodeEnum;
import com.example.demo.pojo.ResponseResult;
import com.example.demo.pojo.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;

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
     * @param:
     * @return:
    */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseResult registerByName(String username, String password, String role) {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(definition);

        if(checkExistByUsername(username)) {
            transactionManager.rollback(status);
            System.out.println(ResponseResult.failure(ResponseErrorCodeEnum.USERNAME_EXIST));
            return ResponseResult.failure(ResponseErrorCodeEnum.USERNAME_EXIST);
        }

        User user = User.builder().username(username).password(password).role(role).build();

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
}
