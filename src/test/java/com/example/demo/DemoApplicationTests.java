package com.example.demo;


import com.example.demo.domain.bean.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    UserService userService;

    @Autowired
    UserMapper userMapper;

    @Test
    void contextLoads() {
        User user = userService.getById("10");
        System.out.println(user.getHasEnable());
        User user1 = userService.getById(10);
        System.out.println(user1.getHasEnable());
        User user2 = userMapper.selectById("10");
        System.out.println(user2.getHasEnable());
        User user3 = userMapper.selectById(10);
        System.out.println(user3.getHasEnable());
    }

}
