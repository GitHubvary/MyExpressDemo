package com.example.demo.controller.courier;

import com.example.demo.domain.bean.User;
import com.example.demo.domain.vo.user.UserInfoVO;
import com.example.demo.service.UserService;
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
        Map<String, Integer> data = userService.getAdminDashboardData();
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


}
