package com.example.demo.domain.bean;

import com.baomidou.mybatisplus.annotation.*;
import com.example.demo.domain.enums.OrderDeleteEnum;
import com.example.demo.domain.enums.OrderStatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Description:订单信息
 * date: 2021/2/24 16:30
 */
@Data
public class OrderInfo {
    @TableId(type = IdType.ID_WORKER_STR)
    private String id;

    private String userId;
    /**
     * 快递单号
     */
    private String odd;
    /**
     * 快递公司ID
     */
    private Integer company;
    /**
     * 收件人
     */
    private String recName;
    /**
     * 收件电话
     */
    private String recTel;
    /**
     * 收货地址
     */
    private String recAddress;
    /**
     * 快递寄达地址
     */
    private String address;
    /**
     * 备注
     */
    private String remark;
    /**
     * 配送员ID
     */
    private String courierId;
    /**
     * 订单状态
     */
    @TableField("status")
    private OrderStatusEnum orderStatus;
    /**
     * 配送员备注
     */
    private String courierRemark;
    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer hasDelete;
    /**
     * 删除原因
     */
    private OrderDeleteEnum deleteType;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createDate;

    @TableField(fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateDate;
}
