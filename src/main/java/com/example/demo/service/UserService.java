package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.pojo.ResponseResult;
import com.example.demo.pojo.User;

public interface UserService extends IService<User> {
    /**
     * Description:根据登录输入信息查找用户
     * @param:用户名和密码
     * @return:用户实体类
    */
    User getByInfo(String username,String password);

    /**
     * Description:根据用户名查找用户信息
     * @param:用户名
     * @return:
    */
    User getByName(String username);

    /**
     * Description:注册信息（用户名、密码和身份）
     * @param:用户名、密码和身份
     * @return:页面返回状态码
    */
    ResponseResult registerByName(String username,String password,String role);

    /**
     * Description:根据用户输入的注册信息判断用户是否存在
     * @param:用户名、密码和身份
     * @return:  存在true,不存在false
    */
    boolean checkExistByUsername(String username);

}
