package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.util.RandomUtils;
import com.example.demo.common.util.StringUtils;
import com.example.demo.domain.ResponseResult;
import com.example.demo.domain.bean.OrderInfo;
import com.example.demo.domain.bean.OrderPayment;
import com.example.demo.domain.bean.User;
import com.example.demo.domain.enums.*;
import com.example.demo.domain.vo.LayuiTableVO;
import com.example.demo.domain.vo.admin.AdminOrderVO;
import com.example.demo.domain.vo.courier.CourierOrderVO;
import com.example.demo.domain.vo.user.OrderDescVO;
import com.example.demo.domain.vo.user.UserOrderVO;
import com.example.demo.mapper.OrderInfoMapper;
import com.example.demo.mapper.OrderPaymentMapper;
import com.example.demo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * date: 2021/2/24 16:44
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderPaymentMapper orderPaymentMapper;

    @Autowired
    private UserService userService;
    @Autowired
    private DataCompanyService dataCompanyService;
    @Autowired
    private OrderPaymentService orderPaymentService;
    @Autowired
    private OrderEvaluateService orderEvaluateService;
    @Autowired
    private DataSourceTransactionManager transactionManager;

    @Override
    public boolean isExistUnfinishedOrder(String userId, UserRoleEnum roleEnum) {
        int count = Integer.MAX_VALUE;
        if(roleEnum == UserRoleEnum.USER) {
            count = orderInfoMapper.selectCount(new QueryWrapper<OrderInfo>()
                    .eq("user_id", userId)
                    .in("status", OrderStatusEnum.WAIT_DIST.getStatus(), OrderStatusEnum.TRANSPORT.getStatus()));
        } else if(roleEnum == UserRoleEnum.COURIER) {
            count = orderInfoMapper.selectCount(new QueryWrapper<OrderInfo>()
                    .eq("courier_id", userId)
                    .in("status",  OrderStatusEnum.TRANSPORT.getStatus()));
        }

        return count != 0;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public ResponseResult createOrder(OrderInfo orderInfo, BigDecimal toPayMoney, BigDecimal AccountMoney, String uid) {
        if (orderInfo.getId()!=null) {
            String id = orderInfo.getId();
            //处理已存在订单
            if (orderInfoMapper.selectById(id) != null) {
                OrderPayment payment = orderPaymentService.getById(id);
                payment.setPayment(toPayMoney);
                if (payment != null && payment.getPaymentStatus() == PaymentStatusEnum.WAIT_BUYER_PAY) {
                    if (toPayMoney.compareTo(AccountMoney) == 1) {
                        System.out.println("已存在订单钱不够");
                        return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
                    } else {
                        payment.setPaymentStatus(PaymentStatusEnum.TRADE_SUCCESS);
                        payment.setPaymentId(RandomUtils.stringWithNumber(4));
                        if (!orderPaymentService.updateById(payment)) {
                            System.out.println("已存在更新失败");
                            return ResponseResult.failure(ResponseErrorCodeEnum.SYSTEM_ERROR);
                        }
                        System.out.println("已存在更新成功");
                        return ResponseResult.success(payment);
                    }
                }
                System.out.println("已存在订单支付表为空或已支付");
                return ResponseResult.failure(ResponseErrorCodeEnum.SYSTEM_ERROR);
            }
        }
        //开启事务创建新订单
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(definition);

        orderInfo.setOrderStatus(OrderStatusEnum.WAIT_DIST);
        orderInfo.setUserId(uid);

        if(!this.retBool(orderInfoMapper.insert(orderInfo))) {
            transactionManager.rollback(status);
            System.out.println("插入失败");
            return ResponseResult.failure(ResponseErrorCodeEnum.ORDER_CREATE_ERROR);
        }
        String orderId = orderInfo.getId();
        if (toPayMoney.compareTo(AccountMoney) == 1){
            boolean a = orderPaymentService.createPayment(orderId, toPayMoney,0);
            if(!a) {
                transactionManager.rollback(status);
                System.out.println("创建未支付失败");
                return ResponseResult.failure(ResponseErrorCodeEnum.ORDER_PAYMENT_CREATE_ERROR);
            }

            transactionManager.commit(status);
            System.out.println("创建未支付成功");
            return ResponseResult.success(orderPaymentService.getById(orderId));
        }


        boolean b = orderPaymentService.createPayment(orderId, toPayMoney,1);
        if(!b) {
            transactionManager.rollback(status);
            System.out.println("创建已支付失败");
            return ResponseResult.failure(ResponseErrorCodeEnum.ORDER_PAYMENT_CREATE_ERROR);
        }

        transactionManager.commit(status);
        System.out.println("创建已支付成功");
        return ResponseResult.success(orderPaymentService.getById(orderId));
    }

    @Override
    public OrderDescVO getDescVO(String orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        if(orderInfo == null) {
            return null;
        }

        OrderDescVO vo = OrderDescVO.builder()
                .orderId(orderId)
                .odd(orderInfo.getOdd())
                .companyName(dataCompanyService.getByCache(orderInfo.getCompany()).getName())
                .recName(orderInfo.getRecName())
                .recTel(orderInfo.getRecTel())
                .address(orderInfo.getAddress())
                .recAddress(orderInfo.getRecAddress())
                .courierTel(orderInfo.getCourierTel())
                .remark(orderInfo.getRemark())
                .orderStatus(orderInfo.getOrderStatus().getName()).build();

        System.out.println("订单详情配送员手机号:"+vo.getCourierTel());
        if(StringUtils.isNotBlank(orderInfo.getCourierId())) {
            String courierFrontName = userService.getFrontName(orderInfo.getCourierId());
            vo.setCourierFrontName(courierFrontName);
            vo.setCourierRemark(orderInfo.getCourierRemark());
            vo.setCourierTel(orderInfo.getCourierTel());
        }

        OrderPayment payment = orderPaymentService.getById(orderId);
        if(payment != null) {
            vo.setPaymentStatus(payment.getPaymentStatus().getName());
            vo.setPaymentType(payment.getPaymentType().getName());
            vo.setPayment(payment.getPayment().toString());
            vo.setPaymentId(payment.getPaymentId());
        }

        return vo;
    }

    @Override
    public boolean isExist(String orderId) {
        return orderInfoMapper.selectById(orderId) != null;
    }

    @Override
    public LayuiTableVO<UserOrderVO> pageUserOrderVO(String userId, Page<UserOrderVO> page, String sql, int isDelete) {
        LayuiTableVO<UserOrderVO> vo = new LayuiTableVO<>();

        IPage<UserOrderVO> selectPage = orderInfoMapper.pageUserOrderVO(page, sql, isDelete);
        System.out.println("获取订单记录"+selectPage.getRecords());

        for(UserOrderVO orderVO : selectPage.getRecords()) {
            // 设置快递公司
            if(StringUtils.isNotBlank(orderVO.getCompany())) {
                orderVO.setCompany(dataCompanyService.getByCache(StringUtils.toInteger(orderVO.getCompany())).getName());
            }
            // 设置是否可以评分
            boolean canEvaluate = orderEvaluateService.canEvaluate(orderVO.getId(), userId, UserRoleEnum.USER);
            if(canEvaluate) {
                orderVO.setCanScore("1");
            } else {
                orderVO.setCanScore("0");
            }
        }

        vo.setTotal(selectPage.getTotal());
        vo.setRows(selectPage.getRecords());

        return vo;
    }

    @Override
    public LayuiTableVO<CourierOrderVO> pageCourierOrderVO(String userId, Page<CourierOrderVO> page, String sql) {
        LayuiTableVO<CourierOrderVO> vo = new LayuiTableVO<>();

        IPage<CourierOrderVO> selectPage = orderInfoMapper.pageCourierOrderVO(page, sql);


        for(CourierOrderVO orderVO : selectPage.getRecords()) {
            // 设置快递公司
            if(StringUtils.isNotBlank(orderVO.getCompany())) {
                orderVO.setCompany(dataCompanyService.getByCache(StringUtils.toInteger(orderVO.getCompany())).getName());
            }

            // 设置是否可以评分
            boolean canEvaluate = orderEvaluateService.canEvaluate(orderVO.getId(), userId, UserRoleEnum.COURIER);
            if(canEvaluate) {
                orderVO.setCanScore("1");
            } else {
                orderVO.setCanScore("0");
            }
        }

        vo.setTotal(selectPage.getTotal());
        vo.setRows(selectPage.getRecords());

        return vo;
    }

    @Override
    public LayuiTableVO<AdminOrderVO> pageAdminOrderVO(Page<AdminOrderVO> page, String sql, int isDelete) {
        LayuiTableVO<AdminOrderVO> vo = new LayuiTableVO<>();

        IPage<AdminOrderVO> selectPage = orderInfoMapper.pageAdminOrderVO(page, sql, isDelete);

//        System.out.println("orderInfoMapper："+orderInfoMapper.pageAdminOrderVO(page, sql, isDelete).getRecords());
        for(AdminOrderVO orderVO : selectPage.getRecords()) {
            // 设置快递公司
            if(StringUtils.isNotBlank(orderVO.getCompany())) {
                orderVO.setCompany(dataCompanyService.getByCache(StringUtils.toInteger(orderVO.getCompany())).getName());
            }
        }

        vo.setTotal(selectPage.getTotal());
        vo.setRows(selectPage.getRecords());

        return vo;
    }

    @Override
    public ResponseResult batchRemoveOrder(String[] ids) {
        int success = 0;
        for(String orderId : ids) {
            OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
            if (orderInfo.getOrderStatus() == OrderStatusEnum.TRANSPORT) {
                continue;
            }
            if(manualDelete(orderId, 1, OrderDeleteEnum.SYSTEM.getType())) {
                success++;
            }
        }

        Map<String, Integer> map = new HashMap<>();
        map.put("success", success);
        map.put("error", ids.length - success);

        return ResponseResult.success(map);
    }

    @Override
    public ResponseResult batchDeleteOrder(String[] ids, String userId) {
        int success = 0;
        for(String orderId : ids) {
            OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
            if(!userId.equals(orderInfo.getUserId())) {
                continue;
            }
            if (orderInfo.getOrderStatus() != OrderStatusEnum.COMPLETE && orderInfo.getOrderStatus() != OrderStatusEnum.ERROR) {
                continue;
            }
            if(manualDelete(orderId, 1, OrderDeleteEnum.MANUAL.getType())) {
                success++;
            }
        }

        Map<String, Integer> map = new HashMap<>();
        map.put("success", success);
        map.put("error", ids.length - success);

        return ResponseResult.success(map);
    }

    @Override
    public ResponseResult batchCancelOrder(String[] ids, String userId) {
        int success = 0;
        for(String orderId : ids) {
            OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
            if(!userId.equals(orderInfo.getUserId())) {
                continue;
            }
            if (orderInfo.getOrderStatus() != OrderStatusEnum.WAIT_DIST && orderInfo.getOrderStatus() != OrderStatusEnum.ERROR) {
                continue;
            }
            if(manualDelete(orderId, 1, OrderDeleteEnum.CANCEL.getType())) {
                success++;
            }
        }
        int finalSuccess = success;
        Map<String, Integer> count = new HashMap<String, Integer>(16) {{
            put("success", finalSuccess);
            put("error", ids.length - finalSuccess);
        }};

        return ResponseResult.success(count);
    }

    @Override
    public ResponseResult batchRollback(String[] ids, String userId) {
        int success = 0;
        for(String orderId : ids) {
            OrderInfo orderInfo = orderInfoMapper.selectByIdIgnoreDelete(orderId);

            if(userId != null && !userId.equals(orderInfo.getUserId())) {
                continue;
            }

            if(manualDelete(orderId, 0, OrderDeleteEnum.CANCEL.getType())) {
                success++;
            }
        }
        int finalSuccess = success;
        Map<String, Integer> count = new HashMap<String, Integer>(16) {{
            put("success", finalSuccess);
            put("error", ids.length - finalSuccess);
        }};

        return ResponseResult.success(count);
    }

    @Override
    public ResponseResult batchAcceptOrder(String[] ids, String userId) {
        int success = 0;
        for(String orderId: ids) {
            OrderInfo orderInfo = orderInfoMapper.selectById(orderId);

            // 限定订单状态为未接单
            if(orderInfo.getOrderStatus() != OrderStatusEnum.WAIT_DIST) {
                continue;
            }

            orderInfo.setCourierId(userId);
            orderInfo.setOrderStatus(OrderStatusEnum.TRANSPORT);
            orderInfo.setCourierTel(userService.getById(userId).getTel());
            if(this.retBool(orderInfoMapper.updateById(orderInfo))) {
                success++;
            }
        }

        int finalSuccess = success;
        Map<String, Integer> count = new HashMap<String, Integer>(16) {{
            put("success", finalSuccess);
            put("error", ids.length - finalSuccess);
        }};

        return ResponseResult.success(count);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public ResponseResult handleOrder(String orderId, OrderStatusEnum targetStatus, String remark) {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(definition);

        OrderInfo orderInfo = getById(orderId);
        if(orderInfo == null) {
            return ResponseResult.failure(ResponseErrorCodeEnum.ORDER_NOT_EXIST);
        }

        OrderStatusEnum originStatus = orderInfo.getOrderStatus();
        // 限定订单状态，非未接单
        if(originStatus == OrderStatusEnum.WAIT_DIST) {
            return ResponseResult.failure(ResponseErrorCodeEnum.OPERATION_ERROR);
        }
        if(originStatus == targetStatus) {
            return ResponseResult.success();
        }

        // 如果原始订单状态为配送中或异常，开启订单评价
        if(originStatus == OrderStatusEnum.TRANSPORT || originStatus == OrderStatusEnum.ERROR) {
            if(!orderEvaluateService.changEvaluateStatus(orderId, true)) {
                transactionManager.rollback(status);
                return ResponseResult.failure(ResponseErrorCodeEnum.OPEN_EVALUATE_ERROR);
            }
        }

        // 更新订单状态
        orderInfo.setOrderStatus(targetStatus);
        if(StringUtils.isNotBlank(remark)) {
            orderInfo.setCourierRemark(remark);
        }
        if(this.retBool(orderInfoMapper.updateById(orderInfo))) {
            transactionManager.commit(status);
            return ResponseResult.success();
        } else {
            transactionManager.rollback(status);
            return ResponseResult.failure(ResponseErrorCodeEnum.OPERATION_ERROR);
        }
    }

    @Override
    public ResponseResult batchAllotOrder(String[] ids, String courierId,String courierTel) {
        int success = 0;
        for(String orderId : ids) {
            OrderInfo orderInfo = orderInfoMapper.selectById(orderId);

            // 限定订单状态，未接单
            if(orderInfo.getOrderStatus() != OrderStatusEnum.WAIT_DIST) {
                continue;
            }
            // 订单状态为支付成功、支付结束
            OrderPayment payment = orderPaymentService.getById(orderId);
            if(payment.getPaymentStatus() != PaymentStatusEnum.TRADE_SUCCESS && payment.getPaymentStatus() != PaymentStatusEnum.TRADE_FINISHED) {
                continue;
            }
            orderInfo.setCourierId(courierId);
            orderInfo.setCourierTel(courierTel);
            System.out.println(orderInfo.getCourierId()+"分配配送员电话："+orderInfo.getCourierTel());
            orderInfo.setOrderStatus(OrderStatusEnum.TRANSPORT);
            if(this.retBool(orderInfoMapper.updateById(orderInfo))) {
                success++;
            }
        }

        int finalSuccess = success;
        Map<String, Integer> count = new HashMap<String, Integer>(16) {{
            put("success", finalSuccess);
            put("error", ids.length - finalSuccess);
        }};

        return ResponseResult.success(count);
    }

    @Override
    public Map<String, Integer> getAdminDashboardData() {
        Map<String, Integer> map = new HashMap<>();

        List<OrderInfo> list = orderInfoMapper.selectList(new QueryWrapper<OrderInfo>().between("create_date",
                LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT), LocalDateTime.now()));


        Integer totalCount = orderInfoMapper.selectCount(null);
        Integer waitCount = orderInfoMapper.selectCount(new QueryWrapper<OrderInfo>().eq("status", OrderStatusEnum.WAIT_DIST.getStatus()));
        Integer transportCount = orderInfoMapper.selectCount(new QueryWrapper<OrderInfo>().eq("status", OrderStatusEnum.TRANSPORT.getStatus()));

        map.put("total",totalCount);
        map.put("today", list.size());
        map.put("wait", waitCount);
        map.put("transport", transportCount);

        return map;
    }

    @Override
    public Map<String, Integer> getUserDashboardData(String userId) {
        Map<String, Integer> map = new HashMap<>();

        List<OrderInfo> orderInfos = orderInfoMapper.selectList(new QueryWrapper<OrderInfo>().eq("user_id", userId));

        int waitCount = 0, transportCount = 0, waitPaymentCount = 0;
        for(OrderInfo info : orderInfos) {
            switch (info.getOrderStatus()) {
                case WAIT_DIST:
                    waitCount++;
                    OrderPayment payment = orderPaymentService.getById(info.getId());
                    if(payment != null && payment.getPaymentStatus() == PaymentStatusEnum.WAIT_BUYER_PAY) {
                        waitPaymentCount++;
                    }
                    break;
                case TRANSPORT:
                    transportCount++;
                    break;
                default:
                    break;
            }
        }
        map.put("waitPayment", waitPaymentCount);
        map.put("wait", waitCount);
        map.put("transport", transportCount);

        return map;
    }

    @Override
    public Map<String, Integer> getCourierDashboardData(String courierId) {
        Map<String, Integer> map = new HashMap<>();

        Integer waitOrderCount = orderInfoMapper.selectCount(new QueryWrapper<OrderInfo>()
                .eq("status", OrderStatusEnum.WAIT_DIST.getStatus()));

        Integer waitPayCount = orderPaymentMapper.selectCount(new QueryWrapper<OrderPayment>()
                .eq("status", PaymentStatusEnum.WAIT_BUYER_PAY.getStatus()));

        Integer waitCount = waitOrderCount - waitPayCount;
        Integer transportCount = orderInfoMapper.selectCount(new QueryWrapper<OrderInfo>()
                .eq("status", OrderStatusEnum.TRANSPORT.getStatus()).eq("courier_id", courierId));
        System.out.println(transportCount);
        map.put("wait", waitCount);
        map.put("transport", transportCount);

        return map;
    }

    @Override
    public boolean manualDelete(String orderId, int hasDelete, int deleteType) {
        return orderInfoMapper.manualDelete(orderId, hasDelete, deleteType);
    }

    @Override
    public boolean isUserOrder(String orderId, String userId) {
        OrderInfo info = getById(orderId);
        if(info == null) {
            return false;
        }
        return info.getUserId().equals(userId);
    }

    @Override
    public boolean isCourierOrder(String orderId, String courierId) {
        OrderInfo info = getById(orderId);
        if(info == null || StringUtils.isBlank(info.getCourierId())) {
            return false;
        }
        return info.getCourierId().equals(courierId);
    }
}
