package com.example.demo.domain.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * Description:快递公司数据
 * date: 2021/2/24 21:24
 */
@Data
public class DataCompany implements Serializable {
    @TableId
    private Integer id;

    private String name;

    private String code;
}
