package com.example.demo.security.handler;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Description:默认认证失败处理
 * date: 2021/2/5 17:45
 */
@Component   //把普通pojo实例化到spring容器中
public class DefaultAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION", exception);
        System.out.println("登录失败");
        this.redirectStrategy.sendRedirect(request, response, "/login");
    }
}
