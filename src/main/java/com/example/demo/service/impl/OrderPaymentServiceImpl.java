package com.example.demo.service.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.util.JsonUtils;
import com.example.demo.config.AliPayConfig;
import com.example.demo.domain.ResponseResult;
import com.example.demo.domain.bean.OrderPayment;
import com.example.demo.domain.enums.PaymentStatusEnum;
import com.example.demo.domain.enums.PaymentTypeEnum;
import com.example.demo.domain.enums.ResponseErrorCodeEnum;
import com.example.demo.exception.CustomException;
import com.example.demo.mapper.OrderPaymentMapper;
import com.example.demo.service.OrderPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 */
@Service
public class OrderPaymentServiceImpl extends ServiceImpl<OrderPaymentMapper, OrderPayment> implements OrderPaymentService {
    @Autowired
    private OrderPaymentMapper orderPaymentMapper;
    @Autowired
    private AliPayConfig aliPayConfig;
    @Autowired
    private AlipayClient alipayClient;

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

    /**
     * 校验订单
     * 支付宝同步/异步回调时调用
     *
     * @author jitwxs
     * @since 2018/6/4 16:40
     */
    @Override
    public boolean validAlipay(Map<String, String> params) throws Exception {
        /* 实际验证过程建议商户务必添加以下校验：
        1、需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
        2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
        3、校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email）
        4、验证app_id是否为该商户本身。
        */

        // 1、调用SDK验证签名
        boolean signVerified = AlipaySignature.rsaCheckV1(params, aliPayConfig.getAlipayPublicKey(), "utf-8", aliPayConfig.getSignType());
        if (!signVerified) {
            return false;
        }
        // 获取订单数据
        String orderId = params.get("out_trade_no");
        OrderPayment payment = orderPaymentMapper.selectById(orderId);
        if (payment == null) {
            return false;
        }
        // 2、判断金额是否相等
        double money = Double.parseDouble(params.get("total_amount"));
        if (new BigDecimal(money).equals(payment.getPayment())) {
            return false;
        }

        // 3、判断商户ID是否相等
        String sellerId = params.get("seller_id");
        if (!sellerId.equals(payment.getSeller())) {
            return false;
        }

        // 4、判断APP_ID是否相等
        String appId = params.get("app_id");
        if (!appId.equals(aliPayConfig.getAppId())) {
            return false;
        }

        return true;
    }

    /*
     * 订单状态（与官方统一）
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

    @Override
    public ResponseResult syncPaymentInfo(String orderId) {
        // 1、设置请求参数
        AlipayTradeQueryRequest alipayRequest = new AlipayTradeQueryRequest();
        Map<String, String> map = new HashMap<>(16);
        map.put("out_trade_no", orderId);
        alipayRequest.setBizContent(JsonUtils.objectToJson(map));

        try {
            // 2、请求
            String json = alipayClient.execute(alipayRequest).getBody();
            Map<String, Object> resMap = JsonUtils.jsonToObject(json, Map.class);
            Map<String, String> responseMap = (Map) resMap.get("alipay_trade_query_response");

            // 获得返回状态码，具体参考：https://docs.open.alipay.com/common/105806
            String code = responseMap.get("code");
            if ("10000".equals(code)) {
                // 获取查询结果
                String paymentStatus = responseMap.get("trade_status");
                String tradeNo = responseMap.get("trade_no");

                OrderPayment payment = orderPaymentMapper.selectById(orderId);

                payment.setPaymentStatus(PaymentStatusEnum.getByDesc(paymentStatus));
                payment.setPaymentId(tradeNo);

                orderPaymentMapper.updateById(payment);

                return ResponseResult.success();
            } else if ("40004".equals(code)) {
                // 交易不存在
                return ResponseResult.failure(ResponseErrorCodeEnum.ORDER_PAYMENT_SYNC_ERROR.getCode(), responseMap.get("sub_msg"));
            } else {
                String errorMsg = responseMap.get("sub_msg");
                return ResponseResult.failure(ResponseErrorCodeEnum.ORDER_PAYMENT_SYNC_ERROR.getCode(), errorMsg);
            }
        } catch (Exception e) {
            throw new CustomException(ResponseErrorCodeEnum.ORDER_PAYMENT_SYNC_ERROR);
        }
    }
}

