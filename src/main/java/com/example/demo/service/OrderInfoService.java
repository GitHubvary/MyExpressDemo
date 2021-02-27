package com.example.demo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.domain.ResponseResult;
import com.example.demo.domain.bean.OrderInfo;
import com.example.demo.domain.enums.OrderStatusEnum;
import com.example.demo.domain.enums.UserRoleEnum;
import com.example.demo.domain.vo.LayuiTableVO;
import com.example.demo.domain.vo.admin.AdminOrderVO;
import com.example.demo.domain.vo.courier.CourierOrderVO;
import com.example.demo.domain.vo.user.OrderDescVO;
import com.example.demo.domain.vo.user.UserOrderVO;

import java.util.Map;

public interface OrderInfoService extends IService<OrderInfo> {
    /**
     * 检查是否有未完成的订单
     * @param roleEnum ROLE_USER: order表userId；ROLE_COURIER:order表courierId
     */
    boolean isExistUnfinishedOrder(String userId, UserRoleEnum roleEnum);

    boolean isExist(String orderId);
    /**
     * 手动删除
     */
    boolean manualDelete(String orderId, int hasDelete, int deleteType);
    /**
     * 是否是某位用户的订单
     */
    boolean isUserOrder(String orderId, String userId);
    /**
     * 是否是某位配送员的订单
     */
    boolean isCourierOrder(String orderId, String courierId);
    /**
     * 生成订单 & 订单支付
     */
    ResponseResult createOrder(OrderInfo orderInfo, double money, String uid);

    OrderDescVO getDescVO(String orderId);

    /**
     * 分页查询订单
     * 适用：普通用户端
     * @param isDelete 0：未删除；1：已删除
     */
    LayuiTableVO<UserOrderVO> pageUserOrderVO(String userId, Page<UserOrderVO> page, String sql, int isDelete);
    /**
     * 分页查询订单
     * 适用：配送员端
     */
    LayuiTableVO<CourierOrderVO> pageCourierOrderVO(String userId, Page<CourierOrderVO> page, String sql);
    /**
     * 分页查询订单
     * 适用：管理员端
     */
    LayuiTableVO<AdminOrderVO> pageAdminOrderVO(Page<AdminOrderVO> page, String sql, int isDelete);
    /**
     * 系统批量删除订单，无需校验userId
     * 订单状态：除【派送中】外所有状态
     */
    ResponseResult batchRemoveOrder(String[] ids);
    /**
     * 批量删除订单
     */
    ResponseResult batchDeleteOrder(String[] ids, String userId);
    /**
     * 批量撤销订单
     */
    ResponseResult batchCancelOrder(String[] ids, String userId);
    /**
     * 批量恢复订单
     * @param userId 为null时，不校验订单所属
     */
    ResponseResult batchRollback(String[] ids, String userId);
    /**
     * 批量接单
     */
    ResponseResult batchAcceptOrder(String[] ids, String userId);
    /**
     * 异常/完成
     */
    ResponseResult handleOrder(String orderId, OrderStatusEnum targetStatus, String remark);
    /**
     * 批量分配订单
     */
    ResponseResult batchAllotOrder(String[] ids, String courierId);

    Map<String, Integer> getAdminDashboardData();

    Map<String, Integer> getUserDashboardData(String userId);

    Map<String, Integer> getCourierDashboardData(String courierId);
}