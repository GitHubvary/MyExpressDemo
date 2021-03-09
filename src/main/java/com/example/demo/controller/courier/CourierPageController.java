package com.example.demo.controller.courier;

import com.example.demo.domain.bean.User;
import com.example.demo.domain.enums.UserRoleEnum;
import com.example.demo.domain.vo.user.UserInfoVO;
import com.example.demo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * Description:配送员页面Controller
 * date: 2021/3/5 9:09
 */
@Controller
@RequestMapping("/courier")
@PreAuthorize("hasRole('ROLE_COURIER')")
public class CourierPageController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderEvaluateService orderEvaluateService;

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private UserFeedbackService feedbackService;

    @Autowired
    private UserEvaluateService userEvaluateService;



    @RequestMapping("/main")
    public String showUserPage(@AuthenticationPrincipal User user, ModelMap map){
        String frontName = user.getUsername();
        map.put("frontName", frontName);

        return "courier/main";
    }

    /**
     * 仪表盘页面
     */
    @RequestMapping("/dashboard")
    public String showDashboardPage(@AuthenticationPrincipal User user, ModelMap map) {
        String score = userEvaluateService.getScoreFromCache(user.getId());
        int evaluateCount = orderEvaluateService.countEvaluate(user.getId(), UserRoleEnum.COURIER);
        map.put("score",score); //评分
        map.put("evaluateCount",evaluateCount);  //评价总数
        Map<String, Integer> data1 = orderInfoService.getCourierDashboardData(user.getId());
        map.put("waitOrder",data1.get("wait"));  //等待接单数
        map.put("transportOrder",data1.get("transport")); //正在派送数
        Map<String, Integer> data2 = feedbackService.getUserDashboardData(user.getId());
        map.put("processFeedback",data2.get("process")); //正在处理反馈数
        map.put("waitFeedback",data2.get("wait")); //等待处理反馈数
        return "courier/dashboard";
    }


    /**
     * Description:用户信息页
     */

    @RequestMapping("/info")
    public String showInfoPage(@AuthenticationPrincipal User user, ModelMap map){
        UserInfoVO userInfo = userService.getUserInfo(user.getId());
        map.put("info", userInfo);
        System.out.println(map);
        return "courier/info";
    }

    /**
     * 接单大厅页面
     */
    @RequestMapping("/order")
    public String showOrderPage(@AuthenticationPrincipal User user) {

        return "courier/order";
    }

    /**
     * 订单列表页面
     */
    @RequestMapping("/history")
    public String showHistory(@AuthenticationPrincipal User user) { return "courier/history"; }

    /**
     * 我的反馈页面
     */
    @RequestMapping("/feedback")
    public String showFeedback() {
        return "courier/feedback";
    }

    /**
     * Description:填写反馈页面
     */

    @RequestMapping("/toFeedback")
    public String showToFeedbackPage(@AuthenticationPrincipal User user){
        return "user/toFeedback";
    }

    /**
     * 评价中心页面
     */
    @RequestMapping("/evaluate")
    public String showEvaluate() {
        return "courier/evaluate";
    }


}
