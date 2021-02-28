package com.example.demo.domain.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息VO，管理员用
 */
@Data
@Builder
public class AdminUserInfoVO implements Serializable {
    private String id;  //用户ID

    private String username;  //用户名

    private String tel;   //手机号

    private Integer role;  //用户权限
    /**
     * 是否实名认证
     */
    private Boolean hasReal;
    /**
     * 是否启用
     */
    private Boolean hasEnable;
    /**
     * 用户评分
     */
    private String score;
    /**
     * 账户解冻时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lockDate;
    /**
     * 注册时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createDate;
}
