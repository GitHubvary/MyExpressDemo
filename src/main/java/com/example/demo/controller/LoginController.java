package com.example.demo.controller;

import com.example.demo.common.StringUtils;
import com.example.demo.pojo.ResponseErrorCodeEnum;
import com.example.demo.pojo.ResponseResult;
import com.example.demo.pojo.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Description:
 * date: 2021/1/16 13:46
 */
@Controller
public class LoginController {


    @Autowired
    private UserService userService;

    /**
     * Description:登录事件处理
     * @param:
     * @return:
    */
    @RequestMapping("/user/login")
    public String login(@RequestParam("username") String username,
                      @RequestParam("password") String password,
                      HttpSession session,Model model){

        User user = userService.getByInfo(username,password);
        if (user != null){
            session.setAttribute("user",user);
            model.addAttribute("flag",null);
            System.out.println("查询成功");
            switch (user.getRole()){  //不同用户身份跳转不同页面
                case "0":
                    return "user";
                case "1":
                    return "courier";
                case "2":
                    return "admin";
                default:
                    return "login";
            }
        } else {
            model.addAttribute("flag","用户名或密码错误");
            System.out.println("查询失败");
             return "login";
        }
    }

    /**
     * Description:注册时间处理
     * @param:
     * @return:
    */
    @PostMapping("/user/register")
    @ResponseBody
    public ResponseResult register(@RequestParam("username") String username,
                                       @RequestParam("password") String password,
                                       @RequestParam("role") String role,
                                       HttpSession session){
        if(StringUtils.isAnyBlank(username, password)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }
        return userService.registerByName(username,password,role);
    }
}
