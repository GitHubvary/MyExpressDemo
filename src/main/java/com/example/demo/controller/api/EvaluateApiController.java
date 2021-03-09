package com.example.demo.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.util.StringUtils;
import com.example.demo.domain.ResponseResult;
import com.example.demo.domain.bean.OrderEvaluate;
import com.example.demo.domain.bean.User;
import com.example.demo.domain.enums.ResponseErrorCodeEnum;
import com.example.demo.service.OrderEvaluateService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.demo.domain.enums.UserRoleEnum.USER;

/**
 * Description:评分列表接口
 * date: 2021/3/9 16:10
 */
@RestController
@RequestMapping("/evaluate")
public class EvaluateApiController {
    @Autowired
    private OrderEvaluateService orderEvaluateService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_COURIER')")
    public ResponseResult listEvaluate(@RequestParam(required = false, defaultValue = "1") Integer current,
                                       @AuthenticationPrincipal User user) {
        if(current < 1) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }

        Page<OrderEvaluate> page = new Page<>(current, 10);
        String userId = user.getId();

        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> record = new ArrayList<>();

        IPage<OrderEvaluate> selectPage;
        switch (user.getRole()) {
            case USER:
                selectPage = orderEvaluateService.page(page, new QueryWrapper<OrderEvaluate>().eq("user_id", userId));
                for(OrderEvaluate evaluate : selectPage.getRecords()) {
                    Map<String, String> map = new HashMap<>();
                    map.put("orderId", evaluate.getId());
                    int score = Double.valueOf(evaluate.getUserScore().toPlainString()).intValue();
                    map.put("score", String.valueOf(score));
                    map.put("courierName",userService.getFrontName(evaluate.getCourierId()));
                    map.put("evaluate", evaluate.getUserEvaluate());
                    record.add(map);
                }
                break;
            case COURIER:
                selectPage = orderEvaluateService.page(page, new QueryWrapper<OrderEvaluate>().eq("courier_id", userId));
                for(OrderEvaluate evaluate : selectPage.getRecords()) {
                    Map<String, String> map = new HashMap<>();
                    map.put("orderId", evaluate.getId());
                    int score = Double.valueOf(evaluate.getUserScore().toPlainString()).intValue();
                    map.put("score",String.valueOf(score));
                    map.put("username",userService.getFrontName(evaluate.getUserId()));
                    map.put("evaluate", evaluate.getUserEvaluate());
                    record.add(map);
                }
                break;
            default:
                return ResponseResult.failure(ResponseErrorCodeEnum.NO_PERMISSION);
        }

        result.put("record", record);
        result.put("current", current);
        result.put("page", selectPage.getPages());
        return ResponseResult.success(result);
    }

}
