package com.example.demo.controller;

import com.example.demo.domain.ResponseResult;
import com.example.demo.domain.bean.User;
import com.example.demo.domain.enums.ResponseErrorCodeEnum;
import com.example.demo.domain.vo.user.UserInfoVO;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Description:
 * date: 2021/1/13 13:56
 */
@Controller
public class PageController {

    @Autowired
    private UserService userService;

    @RequestMapping("/login")
    public String showLoginPage(HttpSession session, HttpServletResponse response, ModelMap map)
    {
        Object exception = session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        if(exception != null) {
            if(exception instanceof BadCredentialsException) {
                map.put("message", ResponseErrorCodeEnum.PASSWORD_ERROR.getMsg());
            } else if (exception instanceof AuthenticationException){
                map.put("message", ((AuthenticationException)exception).getMessage());
            } else if (exception instanceof ResponseResult) {
                map.put("message", ((ResponseResult) exception).getMsg());
            } else if (exception instanceof String) {
                map.put("message", exception);
            }
        }

        session.removeAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        return "login";
    }

    /**
     * 登陆成功页
     */
    @RequestMapping("/")
    public void showSuccessPage(@AuthenticationPrincipal User user, HttpServletResponse response) throws IOException {
        switch (user.getRole()) {
            case DIS_FORMAL:
                response.sendRedirect("/completeInfo");
                return;
            case USER:
                response.sendRedirect("/user/main");
                return;
            case ADMIN:
                response.sendRedirect("/admin/main");
                return;
            case COURIER:
                response.sendRedirect("/courier/main");
                return;
            default:
                response.sendRedirect("/login");
        }
    }


    /**
     * Description:个人信息页
    */

    @RequestMapping("/infoSetting")
    public String showInfoPage(@AuthenticationPrincipal User user, ModelMap map){
        UserInfoVO userInfo = userService.getUserInfo(user.getId());
        map.put("info", userInfo);
        return "/infoSetting";
    }


    @RequestMapping("/register")
    public String showRegister(){
        return "/register";
    }

    @RequestMapping("/menu")
    public String showMenu(){
        return "/menu";
    }

    @RequestMapping("/change")
    public String show(){
        return "changePwd";
    }

    @RequestMapping("/completeInfo")
    public String showCompleteInfoPage() {
        return "completeInfo";
    }
}
