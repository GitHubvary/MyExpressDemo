package com.example.demo.domain.vo.user;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

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

    private String idCard;

    private String realName;

}
