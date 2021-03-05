package com.example.demo.domain.vo.courier;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Description:
 * date: 2021/2/24 16:39
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourierOrderVO  implements Serializable {
    /**
     * 订单号
     */
    private String id;
    /**
     * 快递单号
     */
    private String odd;
    /**
     * 快递公司
     */
    private String company;
    /**
     * 收件人
     */
    private String recName;
    /**
     * 支付金额
     */
    private BigDecimal payment;
    /**
     * 收件电话
     */
    private String recTel;
    /**
     * 快递寄达地址
     */
    private String address;
    /**
     * 收货地址
     */
    private String recAddress;
    /**
     * 备注
     */
    private String remark;
    /**
     * 订单状态
     */
    private Integer orderStatus;
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
