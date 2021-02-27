package com.example.demo.domain.bean;

import com.baomidou.mybatisplus.annotation.*;
import com.example.demo.domain.enums.PaymentStatusEnum;
import com.example.demo.domain.enums.PaymentTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Description:订单支付
 * date: 2021/2/25 10:25
 */
@Data
@Builder
public class OrderPayment {
    @TableId(type = IdType.INPUT)
    private String orderId;
    /**
     * 支付状态
     */
    @TableField("status")
    private PaymentStatusEnum paymentStatus;
    /**
     * 支付方式
     */
    @TableField("type")
    private PaymentTypeEnum paymentType;
    /**
     * 支付金额
     */
    private BigDecimal payment;
    /**
     * 支付流水号
     */
    private String paymentId;
    /**
     * 收款方
     */
    private String seller;
    /**
     * 备注
     */
    private String remark;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createDate;

    @TableField(fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateDate;
}