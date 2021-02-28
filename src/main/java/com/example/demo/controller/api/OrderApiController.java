package com.example.demo.controller.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.util.StringUtils;
import com.example.demo.domain.ResponseResult;
import com.example.demo.domain.bean.User;
import com.example.demo.domain.enums.OrderStatusEnum;
import com.example.demo.domain.enums.PaymentStatusEnum;
import com.example.demo.domain.enums.ResponseErrorCodeEnum;
import com.example.demo.domain.vo.LayuiTableVO;
import com.example.demo.domain.vo.courier.CourierOrderVO;
import com.example.demo.domain.vo.user.OrderDescVO;
import com.example.demo.exception.CustomException;
import com.example.demo.service.OrderInfoService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:订单管理 API接口
 * date: 2021/2/24 16:26
 */
@RestController
@RequestMapping("/order")
public class OrderApiController {
    @Autowired
    private UserService userService;

    @Autowired
    private OrderInfoService orderInfoService;


    @RequestMapping("/test")
    public String showUserDetail(@AuthenticationPrincipal User user) {
        System.out.println("/api/test/user："+user);
        return "/api/test/user";
    }


    /**
     * 获取订单信息
     * - 管理员：任何订单
     * - 派送员：已接的单
     * - 用户：个人订单
     * @author jitwxs
     * @date 2019/4/25 23:36
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_COURIER') or hasRole('ROLE_ADMIN')")
    public ResponseResult getOrderDesc(@PathVariable String id,
                                       @AuthenticationPrincipal User user) {
        // 权限校验
        switch (user.getRole()) {
            case USER:
                if(!orderInfoService.isUserOrder(id, user.getId())) {
                    return ResponseResult.failure(ResponseErrorCodeEnum.NO_PERMISSION);
                }
                break;
            case COURIER:
                if(!orderInfoService.isCourierOrder(id, user.getId())) {
                    return ResponseResult.failure(ResponseErrorCodeEnum.NO_PERMISSION);
                }
                break;
            default:
                break;
        }

        OrderDescVO descVO = orderInfoService.getDescVO(id);
        if(descVO == null) {
            return ResponseResult.failure(ResponseErrorCodeEnum.ORDER_NOT_EXIST);
        }

        return ResponseResult.success(descVO);
    }

    /**
     * 获取所有订单
     * - 普通用户：userId = self
     * - 配送员：courierId = self
     * - 管理员：无限制
     * @param type 0:正常订单；1：已删除订单
     * @author jitwxs
     * @date 2019/4/24 22:21
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_COURIER') or hasRole('ROLE_ADMIN')")
    public LayuiTableVO listSelfOrder(@RequestParam Integer page,
                                      @RequestParam Integer limit,
                                      String type, String startDate, String endDate, String status, String id, String payment,
                                      @AuthenticationPrincipal User user) {
//        System.out.println("/api/order/list："+user);
//        System.out.println("订单列表当前页："+page+"\t每页显示行数："+limit);
//        System.out.println("type："+type+'\t'+"startDate："+startDate+'\t'+"endDate："+endDate+'\t'+"status："+status+'\t'+"id："+id+'\t'+"payment："+payment);
        Integer isDelete = StringUtils.toInteger(type, -1);
        if(isDelete == -1) {
            throw new CustomException(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }
        String userId = user.getId();
        StringBuilder sql = new StringBuilder();

        OrderStatusEnum orderStatusEnum = OrderStatusEnum.getByStatus(StringUtils.toInteger(status, -1));
        if(orderStatusEnum != null) {
            sql.append(" AND info.status = ").append(orderStatusEnum.getStatus());
        }

        PaymentStatusEnum paymentStatusEnum = PaymentStatusEnum.getByStatus(StringUtils.toInteger(payment, -1));
        if(paymentStatusEnum != null) {
            sql.append(" AND payment.status = ").append(paymentStatusEnum.getStatus());
        }

        if(StringUtils.isNotBlank(startDate)) {
            sql.append(" AND info.create_date > '").append(startDate).append("'");
        }

        if(StringUtils.isNotBlank(endDate)) {
            sql.append(" AND info.create_date < '").append(endDate).append("'");
        }

        if(StringUtils.isNotBlank(id)) {
            sql.append(" AND info.id = ").append(id);
        }

        Page orderPage = new Page<>(page, limit);
        orderPage.setDesc("create_date");
        switch (user.getRole()) {
            case USER:
                sql.append(" AND info.user_id = '").append(userId).append("'");
                return orderInfoService.pageUserOrderVO(userId, orderPage, sql.toString(), isDelete);
            case COURIER:
                sql.append(" AND info.courier_id = '").append(userId).append("'");
                return orderInfoService.pageCourierOrderVO(userId, orderPage, sql.toString());
            case ADMIN:
                return orderInfoService.pageAdminOrderVO(orderPage, sql.toString(), isDelete);
            default:
                return new LayuiTableVO();
        }
    }

    /**
     * 获取所有状态为等待接单（支付成功）订单
     */
    @GetMapping("/wait-list")
    @PreAuthorize("hasRole('ROLE_COURIER')")
    public LayuiTableVO<CourierOrderVO> listWaitDistOrder(@RequestParam(required = false, defaultValue = "1") Integer current,
                                                              @RequestParam(required = false, defaultValue = "10") Integer size,
                                                              String startDate, String endDate, String id, @AuthenticationPrincipal User user) {
        Page<CourierOrderVO> page = new Page<>(current, size);
        page.setDesc("create_date");

        StringBuilder sql = new StringBuilder();
        sql.append(" AND info.status = ").append(OrderStatusEnum.WAIT_DIST.getStatus());
        sql.append(" AND payment.status = ").append(PaymentStatusEnum.TRADE_SUCCESS.getStatus());

        if(StringUtils.isNotBlank(startDate)) {
            sql.append(" AND info.create_date > '").append(startDate).append("'");
        }
        if(StringUtils.isNotBlank(endDate)) {
            sql.append(" AND info.create_date < '").append(endDate).append("'");
        }
        if(StringUtils.isNotBlank(id)) {
            sql.append(" AND info.id = ").append(id);
        }
        return orderInfoService.pageCourierOrderVO(user.getId(), page, sql.toString());
    }

    /**
     * 配送员批量接单
     */
    @PostMapping("/batch-accept")
    @PreAuthorize("hasRole('ROLE_COURIER')")
    public ResponseResult batchAccept(String[] ids, @AuthenticationPrincipal User user) {
        return orderInfoService.batchAcceptOrder(ids, user.getId());
    }

    /**
     * 配送员异常订单
     */
    @PostMapping("/error")
    @PreAuthorize("hasRole('ROLE_COURIER')")
    public ResponseResult errorOrder(String id, String remark, @AuthenticationPrincipal User user) {
        if(StringUtils.isAnyBlank(id, remark)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }
        if(remark.length() > 255) {
            return ResponseResult.failure(ResponseErrorCodeEnum.STR_LENGTH_OVER, new Object[]{"异常信息", 255});
        }
        if(!orderInfoService.isCourierOrder(id, user.getId())) {
            return ResponseResult.failure(ResponseErrorCodeEnum.NO_PERMISSION);
        }

        return orderInfoService.handleOrder(id, OrderStatusEnum.ERROR, remark);
    }

    /**
     * 配送员完成订单
     */
    @PostMapping("/complete")
    @PreAuthorize("hasRole('ROLE_COURIER')")
    public ResponseResult completeOrder(String id, String remark, @AuthenticationPrincipal User user) {
        if(StringUtils.isAnyBlank(id, remark)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }
        if(remark.length() > 255) {
            return ResponseResult.failure(ResponseErrorCodeEnum.STR_LENGTH_OVER, new Object[]{"成功信息", 255});
        }
        if(!orderInfoService.isCourierOrder(id, user.getId())) {
            return ResponseResult.failure(ResponseErrorCodeEnum.NO_PERMISSION);
        }

        return orderInfoService.handleOrder(id, OrderStatusEnum.COMPLETE, remark);
    }

    /**
     * 管理员批量订单异常
     */
    @PostMapping("/batch-error")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseResult batchErrorOrder(String[] ids, String remark) {
        if(ids.length == 0 || StringUtils.isBlank(remark)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }
        if(remark.length() > 255) {
            return ResponseResult.failure(ResponseErrorCodeEnum.STR_LENGTH_OVER, new Object[]{"异常信息", 255});
        }

        int success = 0;
        // 循环异常订单，内部事务
        for(String orderId : ids) {
            ResponseResult result = orderInfoService.handleOrder(orderId, OrderStatusEnum.ERROR, remark);
            if(result.getCode() == ResponseErrorCodeEnum.SUCCESS.getCode()) {
                success++;
            }
        }

        // 返回
        Map<String, Integer> count = new HashMap<>(16);
        count.put("success", success);
        count.put("error", ids.length - success);
        return ResponseResult.success(count);
    }

    /**
     * 管理员批量删除订单
     * 相当于普通用户的撤销操作+删除操作
     */
    @PostMapping("/batch-remove")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseResult batchRemoveOrder(String[] ids) {
        if(ids.length == 0) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }
        return orderInfoService.batchRemoveOrder(ids);
    }

    /**
     * 管理员批量分配
     */
    @PostMapping("/batch-allot")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseResult batchAllotOrder(String[] ids, String courier,String courierTel) {
        System.out.println("收到分配："+ids.length);
        if(ids.length == 0 || StringUtils.isBlank(courier)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }
        if(userService.getById(courier) == null) {
            return ResponseResult.failure(ResponseErrorCodeEnum.COURIER_NOT_EXIST);
        }

        return orderInfoService.batchAllotOrder(ids, courier,courierTel);
    }

    /**
     * 管理员批量订单完成
     */
    @PostMapping("/batch-complete")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseResult batchCompleteOrder(String[] ids, String remark) {
        if(ids.length == 0 || StringUtils.isBlank(remark)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }
        if(remark.length() > 255) {
            return ResponseResult.failure(ResponseErrorCodeEnum.STR_LENGTH_OVER, new Object[]{"成功信息", 255});
        }

        int success = 0;
        // 循环完成订单，内部事务
        for(String orderId : ids) {
            ResponseResult result = orderInfoService.handleOrder(orderId, OrderStatusEnum.COMPLETE, remark);
            if(result.getCode() == ResponseErrorCodeEnum.SUCCESS.getCode()) {
                success++;
            }
        }

        // 返回
        Map<String, Integer> count = new HashMap<>(16);
        count.put("success", success);
        count.put("error", ids.length - success);
        return ResponseResult.success(count);
    }

    /**
     * 用户批量删除订单，仅能删除个人订单
     * 状态为订单完成或订单异常
     * @author jitwxs
     * @date 2019/4/24 23:08
     */
    @PostMapping("/batch-delete")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseResult batchDelete(String[] ids, @AuthenticationPrincipal User user) {
        return orderInfoService.batchDeleteOrder(ids, user.getId());
    }

    /**
     * 用户批量撤销订单，仅能撤销个人订单
     * 状态为未接单
     * @author jitwxs
     * @date 2019/4/25 0:11
     */
    @PostMapping("/batch-cancel")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseResult batchCancel(String[] ids, @AuthenticationPrincipal User user) {
        return orderInfoService.batchCancelOrder(ids, user.getId());
    }

    /**
     * 批量恢复订
     * - 普通用户：恢复个人订单
     * - 管理员：恢复任何订单
     */
    @PostMapping("/batch-rollback")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseResult batchRollback(String[] ids, @AuthenticationPrincipal User user) {
        switch (user.getRole()) {
            case ADMIN:
                return orderInfoService.batchRollback(ids, null);
            case USER:
                return orderInfoService.batchRollback(ids, user.getId());
            default:
                return ResponseResult.failure(ResponseErrorCodeEnum.NO_PERMISSION);
        }
    }
}
