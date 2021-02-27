package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.domain.ResponseResult;
import com.example.demo.domain.bean.UserFeedback;
import com.example.demo.domain.enums.FeedbackTypeEnum;
import com.example.demo.domain.vo.LayuiTableVO;
import com.example.demo.domain.vo.user.UserFeedbackVO;

import java.util.Map;

public interface UserFeedbackService extends IService<UserFeedback> {
    /**
     * Description:前台分页查询发送反馈数据列表
    */
    LayuiTableVO<UserFeedbackVO> pageUserFeedbackVO(Page<UserFeedback> page, QueryWrapper<UserFeedback> wrapper);

    boolean createFeedback(String userId, FeedbackTypeEnum feedbackTypeEnum, String content, String orderId);

    ResponseResult batchCancelOrder(String[] ids, String id);

    Map<String, Integer> getAdminDashboardData();

    Map<String, Integer> getUserDashboardData(String userId);

    Map<String, Integer> getCourierDashboardData();
}
