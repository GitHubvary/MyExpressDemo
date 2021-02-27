package com.example.demo.domain.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Description:
 * date: 2021/2/24 16:38
 */
@Data
@Builder
public class AdminOrderVO {
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
     * 支付状态
     */
    private Integer paymentStatus;
    /**
     * 删除原因
     */
    private Integer deleteType;
    /**
     * 下单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createDate;
}
