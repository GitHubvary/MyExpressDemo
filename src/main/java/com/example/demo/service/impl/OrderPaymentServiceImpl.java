package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.domain.bean.OrderPayment;
import com.example.demo.domain.enums.PaymentStatusEnum;
import com.example.demo.domain.enums.PaymentTypeEnum;
import com.example.demo.mapper.OrderPaymentMapper;
import com.example.demo.service.OrderPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Description:
 */
@Service
public class OrderPaymentServiceImpl extends ServiceImpl<OrderPaymentMapper, OrderPayment> implements OrderPaymentService {
    @Autowired
    private OrderPaymentMapper orderPaymentMapper;

    @Override
    public boolean createAliPayment(String orderId, double money, String sellerId) {
        OrderPayment payment = OrderPayment.builder()
                .orderId(orderId)
                .paymentStatus(PaymentStatusEnum.WAIT_BUYER_PAY)
                .paymentType(PaymentTypeEnum.AliPay)
                .payment(new BigDecimal(money))
                .seller(sellerId).build();

        return this.retBool(orderPaymentMapper.insert(payment));
    }

    /*
     * 订单状态
     * WAIT_BUYER_PAY：交易创建，等待买家付款；
     * TRADE_CLOSED：未付款交易超时关闭，或支付完成后全额退款；
     * TRADE_SUCCESS：交易支付成功；
     * TRADE_FINISHED：支付结束，不可退款
     */

    @Override
    public boolean updateStatus(String orderId, PaymentStatusEnum status, String... tradeNo) {
        // 判断参数是否合法
        OrderPayment payment = orderPaymentMapper.selectById(orderId);
        if (payment == null) {
            return false;
        }

        // 如果订单状态相同，不发生改变
        if (status == payment.getPaymentStatus()) {
            return true;
        }

        // 如果是TRADE_SUCCESS，设置第三方订单号
        if (PaymentStatusEnum.TRADE_SUCCESS.equals(status) && tradeNo.length > 0) {
            payment.setPaymentId(tradeNo[0]);
        }

        payment.setPaymentStatus(status);

        return this.retBool(orderPaymentMapper.updateById(payment));
    }

}

