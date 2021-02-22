package com.example.demo.security;

import com.example.demo.security.handler.DefaultAuthenticationFailureHandler;
import com.example.demo.security.validate.TraditionSecurityConfig;
import com.example.demo.security.validate.TraditionUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Description:Spring Security 核心配置类
 * date: 2021/2/3 16:21
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{

    @Autowired
    private TraditionUserDetailsService userDetailService;

    @Autowired
    private DefaultAuthenticationFailureHandler defaultAuthenticationFailureHandler;


    @Autowired
    private TraditionSecurityConfig traditionSecurityConfig;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
            http
                    .apply(traditionSecurityConfig).and()
                    .formLogin()
                    // 设置登陆页
                    .loginPage("/login")
                    // 设置登陆成功/失败处理逻辑
//                    .loginProcessingUrl("index")
                    .defaultSuccessUrl("/")
                    .failureHandler(defaultAuthenticationFailureHandler)
                    .permitAll().and()
                    .logout()
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login")
                    .deleteCookies("JSESSIONID")
                    .and().authorizeRequests()
                      .antMatchers("/register","/form-login","/user/register").permitAll()
                    .anyRequest().authenticated()
                    .and().csrf().disable()
                    .headers().frameOptions().sameOrigin();

    }


    /**
     * 配置密码加密方式，这里选择不加密
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailService).passwordEncoder(passwordEncoder());
    }

   @Override
    public void configure(WebSecurity web) throws Exception {
        // 设置拦截忽略文件夹，可以对静态资源放行
        web
                .ignoring()
                .antMatchers("/api/**","/css/**","/images/**","/js/**","/layui/**","/lib/**","/page/**");
    }
}
