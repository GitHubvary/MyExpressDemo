package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Description:
 * date: 2021/1/13 13:56
 */
@Controller
public class PageController {
    @RequestMapping({"/","/login"})
    public String showIndex(){
        return "login";
    }

    @RequestMapping("/register")
    public String showRegister(){
        return "register";
    }
}
