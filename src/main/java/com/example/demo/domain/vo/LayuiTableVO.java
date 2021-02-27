package com.example.demo.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Description:
 * date: 2021/2/19 18:17
 */
@Data
public class LayuiTableVO<T> implements Serializable {
    /**
     * 总记录数
     */
    private Long total;
    /**
     * 记录
     */
    private List<T> rows;
}