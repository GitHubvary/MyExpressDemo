package com.example.demo.domain.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Description:
 * date: 2021/2/24 16:40
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOrderVO implements Serializable {
    /**
     * 订单号
     */
    private String id;
    /**
     * 取货码
     */
    private String odd;
    /**
     * 支付码
     */
    private String paymentId;
    /**
     * 快递公司
     */
    private String company;
    /**
     * 支付状态
     */
    private Integer paymentStatus;
    /**
     * 支付金额
     */
    private String payment;
    /**
     * 订单状态
     */
    private Integer orderStatus;
    /**
     * 删除原因
     */
    private Integer deleteType;
    /**
     * 能否评分，1可以，0可以
     */
    private String canScore;
    /**
     * 下单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createDate;
}
