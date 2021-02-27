package com.example.demo.controller.api;

import com.example.demo.domain.bean.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 * date: 2021/2/25 20:43
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @RequestMapping("/user")
    public String showUserDetail(@AuthenticationPrincipal User user) {
        System.out.println("test/user："+user);
        return "test/user";
    }
}
