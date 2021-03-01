package com.example.demo.controller.user;

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
 * Description:
 * date: 2021/3/1 15:52
 */
@Controller
@RequestMapping("/user")
@PreAuthorize("hasRole('ROLE_USER')")
public class UserPageController {

    @Autowired
    private UserService userService;


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
        String frontName = user.getUsername();
        map.put("frontName", frontName);

        Map<String, Integer> data = userService.getAdminDashboardData();

        map.put("userTodayCount", data.get("today"));
        map.put("userTotalCount", data.get("total"));
        map.put("userDisableCount", data.get("disEnable"));
        map.put("userLockCount", data.get("lock"));

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
    public String showPlacePage(@AuthenticationPrincipal User user, ModelMap map){
        String frontName = user.getUsername();
        map.put("frontName", frontName);
        return "user/placeOrder";
    }

    /**
     * Description:下单页面
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
     * 评价反馈页面
     */
    @RequestMapping("/feedback")
    public String showFeedback() {
        return "user/feedback";
    }

}
