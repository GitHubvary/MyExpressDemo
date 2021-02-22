package com.example.demo.domain.vo.admin;

/**
 * Description:
 * date: 2021/2/19 18:16
 */

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
    private Integer id;

    private String username;

    private String tel;

    private Integer role;
    /**
     * 是否实名认证
     */
    private Boolean hasReal;
    /**
     * 是否启用
     */
    private Boolean hasEnable;
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
