package com.example.demo.security.validate;

import com.example.demo.common.util.DateUtils;
import com.example.demo.domain.bean.User;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Description:
 * date: 2021/2/5 18:04
 */
public class TraditionAuthenticationProvider implements AuthenticationProvider {
    private UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException{
        TraditionAuthenticationToken authenticationToken = (TraditionAuthenticationToken) authentication;

        String inputName = (String) authenticationToken.getPrincipal();
        String inputPassword = (String) authenticationToken.getCredentials();

        // userDetails为数据库中查询到的用户信息
        UserDetails userDetails = userDetailsService.loadUserByUsername(inputName);

        //密码和数据库加密的密文校验
//        // 如果是自定义AuthenticationProvider，需要手动密码校验
//        if (!new BCryptPasswordEncoder().matches(inputPassword, userDetails.getPassword())) {
//            throw new BadCredentialsException("密码错误");
//        }

        //普通校验
        // 如果是自定义AuthenticationProvider，需要手动密码校验
        if (!inputPassword.equals(userDetails.getPassword())) {
            throw new BadCredentialsException("密码错误");
        }

        // 校验账户状态
        authenticationChecks(userDetails);

        // 此时鉴权成功后，应当重新 new 一个拥有鉴权的 authenticationResult 返回
        TraditionAuthenticationToken authenticationResult = new TraditionAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        authenticationResult.setDetails(authenticationToken.getDetails());

        return authenticationResult;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // 这里不要忘记，和UsernamePasswordAuthenticationToken比较
        return authentication.equals(TraditionAuthenticationToken.class);
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    private void authenticationChecks(UserDetails user) {
        if (!user.isAccountNonLocked()) {

            throw new LockedException("账户已冻结，解冻时间为：" + DateUtils.format(((User)user).getLockDate(), 1));
        }

        if (!user.isEnabled()) {

            throw new DisabledException("账户已失效，请联系管理员");
        }

    }


}
