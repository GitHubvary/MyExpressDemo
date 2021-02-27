package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.domain.bean.OrderInfo;
import com.example.demo.domain.vo.admin.AdminOrderVO;
import com.example.demo.domain.vo.courier.CourierOrderVO;
import com.example.demo.domain.vo.user.UserOrderVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
    OrderInfo selectByIdIgnoreDelete(String id);

    boolean manualDelete(@Param("id") String orderId, @Param("hasDelete") int hasDelete, @Param("deleteType") int deleteType);

    IPage<UserOrderVO> pageUserOrderVO(Page<UserOrderVO> page, @Param("sql") String selectSql, @Param("has_delete") int isDelete);

    IPage<CourierOrderVO> pageCourierOrderVO(Page<CourierOrderVO> page, @Param("sql") String sql);

    // WHERE info.id = payment.order_id AND info.has_delete = #{has_delete} ${sql}
    /**
     * Description:联表查询，order_info和order_payment表必须有一样id的记录才可以查询出值
    */
    IPage<AdminOrderVO> pageAdminOrderVO(Page<AdminOrderVO> page, @Param("sql")  String sql, @Param("has_delete") int isDelete);

}
