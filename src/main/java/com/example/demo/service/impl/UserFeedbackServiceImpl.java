package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.util.CollectionUtils;
import com.example.demo.common.util.RandomUtils;
import com.example.demo.common.util.StringUtils;
import com.example.demo.domain.ResponseResult;
import com.example.demo.domain.bean.UserFeedback;
import com.example.demo.domain.enums.FeedbackStatusEnum;
import com.example.demo.domain.enums.FeedbackTypeEnum;
import com.example.demo.domain.vo.LayuiTableVO;
import com.example.demo.domain.vo.user.UserFeedbackVO;
import com.example.demo.mapper.UserFeedbackMapper;
import com.example.demo.service.OrderInfoService;
import com.example.demo.service.UserFeedbackService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Description:用户反馈接口实现类
 * date: 2021/2/25 9:11
 */
@Service
public class UserFeedbackServiceImpl extends ServiceImpl<UserFeedbackMapper, UserFeedback> implements UserFeedbackService {
    @Autowired
    private UserFeedbackMapper userFeedbackMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderInfoService orderInfoService;

    @Override
    public LayuiTableVO<UserFeedbackVO> pageUserFeedbackVO(Page<UserFeedback> page, QueryWrapper<UserFeedback> wrapper) {

        IPage<UserFeedback> selectPage = userFeedbackMapper.selectPage(page, wrapper);

        LayuiTableVO<UserFeedbackVO> vo = new LayuiTableVO<>();
        vo.setTotal(selectPage.getTotal());
        vo.setRows(convert(selectPage.getRecords()));

        return vo;
    }

    @Override
    public boolean createFeedback(String userId, FeedbackTypeEnum feedbackTypeEnum, String content, String orderId) {
        UserFeedback feedback = UserFeedback.builder()
                .id(RandomUtils.time())
                .userId(userId)
                .feedbackType(feedbackTypeEnum)
                .feedbackStatus(FeedbackStatusEnum.WAIT)
                .content(content).build();
        if(StringUtils.isNotBlank(orderId)) {
            feedback.setOrderId(orderId);
        }

        return this.retBool(userFeedbackMapper.insert(feedback));
    }

    @Override
    public ResponseResult batchCancelOrder(String[] ids, String userId) {
        int success = 0;
        for(String id : ids) {
            UserFeedback feedback = userFeedbackMapper.selectById(id);
            if(!userId.equals(feedback.getUserId())) {
                continue;
            }
            if (feedback.getFeedbackStatus() != FeedbackStatusEnum.WAIT) {
                continue;
            }
            if(this.retBool(userFeedbackMapper.deleteById(id))) {
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
        Integer todayCount = userFeedbackMapper.selectCount(new QueryWrapper<UserFeedback>().between("create_date",
                LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT), LocalDateTime.now()));

        Integer waitCount = userFeedbackMapper.selectCount(new QueryWrapper<UserFeedback>().eq("status", FeedbackStatusEnum.WAIT.getStatus()));
        Integer totalCount = userFeedbackMapper.selectCount(null);
        Integer processCount = userFeedbackMapper.selectCount(new QueryWrapper<UserFeedback>().eq("status", FeedbackStatusEnum.PROCESS.getStatus()));
        map.put("process",processCount);
        map.put("total",totalCount);
        map.put("today", todayCount);
        map.put("wait", waitCount);
        return map;
    }

    @Override
    public Map<String, Integer> getUserDashboardData(String userId) {
        Map<String, Integer> map = new HashMap<>();
        Integer waitCount = userFeedbackMapper.selectCount(new QueryWrapper<UserFeedback>().eq("status", FeedbackStatusEnum.WAIT.getStatus()).eq("user_id", userId));
        Integer processCount = userFeedbackMapper.selectCount(new QueryWrapper<UserFeedback>().eq("status", FeedbackStatusEnum.PROCESS.getStatus()).eq("user_id", userId));

        map.put("wait", waitCount);
        map.put("process", processCount);
        return map;
    }

    @Override
    public Map<String, Integer> getCourierDashboardData() {
        Map<String, Integer> map = new HashMap<>();
        Integer todayCount = userFeedbackMapper.selectCount(new QueryWrapper<UserFeedback>().between("create_date",
                LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT), LocalDateTime.now()));

        Integer waitCount = userFeedbackMapper.selectCount(new QueryWrapper<UserFeedback>().eq("status", FeedbackStatusEnum.WAIT.getStatus()));

        map.put("today", todayCount);
        map.put("wait", waitCount);
        return map;
    }

    private List<UserFeedbackVO> convert(List<UserFeedback> userFeedbacks) {
        if (CollectionUtils.isListEmpty(userFeedbacks)) {
            return Collections.emptyList();
        }
        return userFeedbacks.stream().map(this::convert).collect(Collectors.toList());
    }

    private UserFeedbackVO convert(UserFeedback userFeedback) {

        String frontName = userService.getFrontName(userFeedback.getUserId());

        UserFeedbackVO vo = UserFeedbackVO.builder()
                .id(userFeedback.getId())
                .frontName(frontName)
                .type(userFeedback.getFeedbackType().getType())
                .status(userFeedback.getFeedbackStatus().getStatus())
                .content(userFeedback.getContent())
                .createDate(userFeedback.getCreateDate())
                .updateDate(userFeedback.getUpdateDate()).build();

        if(StringUtils.isNotBlank(userFeedback.getOrderId())) {
            vo.setOrderId(userFeedback.getOrderId());
        }

        if(StringUtils.isNotBlank(userFeedback.getHandler())) {
            String handlerName = userService.getFrontName(userFeedback.getHandler());
            vo.setHandlerName(handlerName);
            vo.setResult(userFeedback.getResult());
        }

        return vo;
    }
}
