package com.example.demo.controller.admin;

import com.example.demo.common.constant.RedisKeyConstant;
import com.example.demo.domain.ResponseResult;
import com.example.demo.domain.bean.User;
import com.example.demo.domain.enums.ResponseErrorCodeEnum;
import com.example.demo.domain.vo.user.UserInfoVO;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Description:管理员界面Controller
 * date: 2021/2/6 18:39
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminPageController {

    @Autowired
    private UserService userService;


    @RequestMapping("/main")
    public String showAdminPage(@AuthenticationPrincipal User user, ModelMap map){
        String frontName = user.getUsername();
        map.put("frontName", frontName);

        return "admin/main";
    }

    /**
     * 仪表盘页面
     */
    @RequestMapping("/dashboard")
    public String showDashboardPage(@AuthenticationPrincipal User user, ModelMap map) {
        String frontName = user.getUsername();
        System.out.println("admin/dashboard："+frontName);
        map.put("frontName", frontName);

        Map<String, Integer> data = userService.getAdminDashboardData();

        map.put("userTodayCount", data.get("today"));
        map.put("userTotalCount", data.get("total"));
        map.put("userDisableCount", data.get("disEnable"));
        map.put("userLockCount", data.get("lock"));

        return "admin/dashboard";
    }

    /**
     * Description:管理员信息页
     */

    @RequestMapping("/info")
    public String showInfoPage(@AuthenticationPrincipal User user, ModelMap map){
        UserInfoVO userInfo = userService.getUserInfo(user.getId());
        map.put("info", userInfo);
        System.out.println(map);
        return "admin/info";
    }

    /**
     * Description:管理员信息修改
     */
    @PostMapping("/changeAdminInfo")
    @ResponseBody
    public ResponseResult changeAdminInfo(@AuthenticationPrincipal User user,String tel) {
        System.out.println("修改前的号码："+user.getTel());
        user.setTel(tel);
        if(!userService.updateById(user)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.OPERATION_ERROR);
        }
        System.out.println("修改后的号码："+user.getTel());
        return ResponseResult.success();
    }

    /**
     * 用户管理页面
     */
    @RequestMapping("/userList")
    public String showUser() {
        return "admin/userList";
    }

    /**
     * 订单管理页面
     */
    @RequestMapping("/order")
    public String showOrder() {
        return "admin/order";
    }

    /**
     * 反馈管理页面
     */
    @RequestMapping("/feedback")
    public String showFeedback() {
        return "admin/feedback";
    }

    /**
     * 订单回收页面
     */
    @RequestMapping("/recycle")
    public String showRecycle() {
        return "admin/recycle";
    }

}
