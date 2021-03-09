package com.example.demo.controller.user;

import com.example.demo.domain.bean.OrderInfo;
import com.example.demo.domain.bean.User;
import com.example.demo.domain.vo.user.UserInfoVO;
import com.example.demo.service.DataCompanyService;
import com.example.demo.service.OrderInfoService;
import com.example.demo.service.UserFeedbackService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Description:用户页面Controller
 * date: 2021/3/1 15:52
 */
@Controller
@RequestMapping("/user")
@PreAuthorize("hasRole('ROLE_USER')")
public class UserPageController {

    @Autowired
    private UserService userService;

    @Autowired
    private DataCompanyService dataCompanyService;

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private UserFeedbackService feedbackService;


    @RequestMapping("/main")
    public String showUserPage(@AuthenticationPrincipal User user, ModelMap map){
        String frontName = user.getUsername();
        map.put("frontName", frontName);

        return "user/main";
    }

    /**
     * 仪表盘页面
     */
    @RequestMapping("/dashboard")
    public String showDashboardPage(@AuthenticationPrincipal User user, ModelMap map) {
        Map<String, Integer> data1 = orderInfoService.getUserDashboardData(user.getId());
        Map<String, Integer> data2 = feedbackService.getUserDashboardData(user.getId());
        map.put("waitPayment",data1.get("waitPayment")); //未支付订单
        map.put("waitOrder",data1.get("wait"));  //等待接单订单
        map.put("transportOrder",data1.get("transport")); //正在配送订单
        map.put("processFeedback",data2.get("process")); //正在处理反馈
        map.put("waitFeedback",data2.get("wait")); //等待处理反馈
        return "user/dashboard";
    }

    /**
     * Description:用户信息页
     */

    @RequestMapping("/info")
    public String showInfoPage(@AuthenticationPrincipal User user, ModelMap map){
        UserInfoVO userInfo = userService.getUserInfo(user.getId());
        map.put("info", userInfo);
        System.out.println(map);
        return "user/info";
    }

    /**
     * Description:下单页面
     */

    @RequestMapping("/place")
    public String showPlacePage(@AuthenticationPrincipal User user){
        return "user/placeOrder";
    }

    /**
     * Description:下单页面
     */

    @RequestMapping("/toFeedback")
    public String showToFeedbackPage(@AuthenticationPrincipal User user){
        return "user/toFeedback";
    }

    /**
     * Description:历史订单
     */

    @RequestMapping("/history")
    public String showHistoryPage(@AuthenticationPrincipal User user, ModelMap map){
        String frontName = user.getUsername();
        map.put("frontName", frontName);
        return "user/history";
    }

    /**
     * 订单回收页面
     */
    @RequestMapping("/recycle")
    public String showRecycle() {
        return "user/recycle";
    }

    /**
     * 我的反馈页面
     */
    @RequestMapping("/feedback")
    public String showFeedback() {
        return "user/feedback";
    }

    @RequestMapping("/toPay")
    public String placeOrder(OrderInfo orderInfo, ModelMap map, HttpSession session, @AuthenticationPrincipal User user) {
        map.put("frontName", userService.getFrontName(user));
        map.put("order", orderInfo);
        map.put("account",user.getAccount());
        map.put("company", dataCompanyService.getByCache(orderInfo.getCompany()).getName());
        session.setAttribute("SESSION_LATEST_EXPRESS", orderInfo);
        System.out.println(session.getAttribute("SESSION_LATEST_EXPRESS"));
        return "user/payment";
    }

    /**
     * 评价中心页面
     */
    @RequestMapping("/evaluate")
    public String showEvaluate() {

        return "user/evaluate";
    }

}
