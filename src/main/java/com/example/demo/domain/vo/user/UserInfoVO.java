package com.example.demo.domain.vo.user;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Description:
 * date: 2021/2/17 11:01
 */
@Data
@Builder
public class UserInfoVO implements Serializable {
    private String username;

    private String tel;

    private String school;

    private String studentId;

    private String role;

    private String roleName;

    private BigDecimal account;

    private String idCard;

    private String realName;

}
