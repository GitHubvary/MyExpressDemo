package com.example.demo.domain.bean;

import com.baomidou.mybatisplus.annotation.*;
import com.example.demo.domain.enums.UserRoleEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Description:
 * date: 2021/1/16 14:37
 */
@Data
@Builder
@TableName("t_user") //标注表名
public class User implements UserDetails, CredentialsContainer {

    //标注主键
    @TableId(type = IdType.UUID)
    private String id;


    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("role_id")
    @JsonValue
    private UserRoleEnum role;  //用户身份

    @TableField("real_name")
    private String realName;  //真实姓名

    @TableField("id_card")
    private String idCard;  //身份证号

    @TableField("student_id")
    private String studentId;//学号

    @TableField("tel")
    private String tel; //手机号

    @TableField("school_name")
    private String schoolName;  //学校名称

    @Version
    @TableField("version")
    private Integer version;



     // 解冻时间

    @TableField("lock_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lockDate;


    @TableField(value = "create_date",fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createDate;

    @TableField(value = "update_date",fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateDate;



    /**
     * 是否启用
     * 1：启用；0：禁用
     */
    private Integer hasEnable;



    /**
     * 是否未冻结
     */
    @Override
    public boolean isAccountNonLocked() {
        if(this.lockDate == null) {
            return true;
        }

        return LocalDateTime.now().isAfter(this.lockDate);
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<GrantedAuthority>(){{
            add(new SimpleGrantedAuthority(getRole().getName()));
        }};
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }


    /**
     * Description:凭证是否过期
    */
    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    /**
     * Description:是否启用
    */
    @Override
    public boolean isEnabled() {
        return this.hasEnable == 1;
    }
}
