package com.example.demo.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

/**
 * Description:
 * date: 2021/1/16 14:37
 */
@Data
@Builder
@TableName("t_user") //标注表名
public class User {

    //标注主键
    @TableId(type = IdType.AUTO)
    private Integer uid;


    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("role")  //用户身份：'0'代表普通用户，'1'代表配送员,'2'代表管理员
    private String role;


}
