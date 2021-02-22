package com.example.demo.domain.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;
import lombok.Getter;

import java.util.Arrays;

/**
 * Description:系统用户角色枚举
*/

@Getter
public enum UserRoleEnum implements IEnum<Integer> {   //继承mybatis plus的IEnum接口
    DIS_FORMAL(0, "DIS_FORMAL", "非正式用户"),  //没有完善信息的用户
    ADMIN(3, "ROLE_ADMIN", "系统管理员"),
    COURIER(2, "ROLE_COURIER", "配送员"),
    USER(1, "ROLE_USER", "普通用户");

    private int type;

    private String name;

    private String cnName;

    UserRoleEnum(int type, String name, String cnName) {
        this.type = type;
        this.name = name;
        this.cnName = cnName;
    }

    @Override
    public Integer getValue() {
        return this.type;
    }

    /**
     * Description:通过用户角色id获取枚举
     */
    public static UserRoleEnum getByType(int type) {
        return Arrays.stream(values()).filter(e -> e.getType() == type).findFirst().orElse(null);
    }


    public static UserRoleEnum getByName(String name) {
        return Arrays.stream(values()).filter(e -> e.getName().equals(name)).findFirst().orElse(null);
    }
}
