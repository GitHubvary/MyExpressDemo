package com.example.demo.controller;

import com.example.demo.common.util.StringUtils;
import com.example.demo.domain.bean.DataCompany;
import com.example.demo.domain.bean.OrderInfo;
import com.example.demo.domain.bean.OrderPayment;
import com.example.demo.domain.enums.PaymentStatusEnum;
import com.example.demo.domain.enums.ResponseErrorCodeEnum;
import com.example.demo.domain.ResponseResult;
import com.example.demo.domain.bean.User;
import com.example.demo.domain.enums.UserRoleEnum;
import com.example.demo.exception.CustomException;
import com.example.demo.service.DataCompanyService;
import com.example.demo.service.OrderInfoService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;

/**
 * Description:
 * date: 2021/1/16 13:46
 */
@Controller
public class EventController {


    @Autowired
    private UserService userService;

    @Autowired
    private DataCompanyService dataCompanyService;

    @Autowired
    private OrderInfoService orderInfoService;

    /**
     * Description:注册事件处理
     * @param:用户注册
     * @return:注册结果
    */
    @SuppressWarnings("unchecked")
    @PostMapping("/user/register")
    @ResponseBody
    public ResponseResult register(@RequestParam("username") String username, @RequestParam("password") String password)
    {
        if(StringUtils.isAnyBlank(username, password)) {
            System.out.println("注册失败！");
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }
        System.out.println("注册成功！");
        return userService.registerByName(username,password);
    }

    /**
     * Description:修改密码
    */
    @PostMapping("/password")
    @ResponseBody
    public ResponseResult resetPassword(@RequestParam("old_password") String origin, @RequestParam("new_password") String target, @AuthenticationPrincipal User user) {
        if(StringUtils.isAnyBlank(origin, target)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }
        return userService.resetPassword(user.getId(),origin,target);
    }

    /**
     * Description:注册用户第一次登录完善信息
    */
    @PostMapping("/user/completeInfo")
    @ResponseBody
    public ResponseResult completeInfo(Integer role,
                                       String schoolName,
                                       String realName,
                                       String studentId,
                                       String idCard,
                                       String tel,
                                       @AuthenticationPrincipal User user)
    {

        if(role == null || schoolName == null || StringUtils.isBlank(tel)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }

        // 仅支持申请普通用户、配送员
        UserRoleEnum roleEnum = UserRoleEnum.getByType(role);
        if(roleEnum != UserRoleEnum.USER && roleEnum != UserRoleEnum.COURIER) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }

        user.setRole(roleEnum);
        user.setSchoolName(schoolName);
        user.setTel(tel);

        // 配送员必填真实姓名、身份证号
        if(roleEnum == UserRoleEnum.COURIER) {
            if(StringUtils.isAnyBlank(realName, idCard)) {
                return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
            }

//          // 校验学号
            if(!StringUtils.isNumeric(studentId)) {
                return ResponseResult.failure(ResponseErrorCodeEnum.STUDENT_IDCARD_NOT_NUMBER);
            }


            if(StringUtils.containsSpecial(realName) || StringUtils.containsNumber(realName)) {
                return ResponseResult.failure(ResponseErrorCodeEnum.REAL_NAME_INVALID);
            }

            user.setStudentId(studentId);
            user.setIdCard(idCard);
            user.setRealName(realName);
        }

        if(!userService.updateById(user)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.OPERATION_ERROR);
        }
        System.out.println("完善信息用户："+user);
        return ResponseResult.success();
    }

    /**
     * Description:充值或撤销，账户打钱
    */
    @PostMapping("/addMoney")
    @ResponseBody
    public ResponseResult addAccount(@AuthenticationPrincipal User user,String money){
        BigDecimal addMoney = new BigDecimal(money);
        BigDecimal account = user.getAccount().add(addMoney);
        BigDecimal maxAccount = new BigDecimal(10000000);
        if (account.compareTo(maxAccount) == 1){
            System.out.println("用户充值超过最大限额。");
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }
        user.setAccount(account);
        if (!userService.updateById(user)){
            return ResponseResult.failure(ResponseErrorCodeEnum.OPERATION_ERROR);
        }
        return ResponseResult.success(user.getAccount());
    }

    /**
     * Description:提现或还原，账户扣钱
    */
    @PostMapping("/removeMoney")
    @ResponseBody
    public ResponseResult removeAccount(@AuthenticationPrincipal User user,String money){
        BigDecimal reMoney = new BigDecimal(money);
        BigDecimal account = user.getAccount().subtract(reMoney);
        BigDecimal minAccount = new BigDecimal(0);
        if (account.compareTo(minAccount) == -1){
            System.out.println("用户扣款超过最大限额。");
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }
        user.setAccount(account);
        if (!userService.updateById(user)){
            return ResponseResult.failure(ResponseErrorCodeEnum.OPERATION_ERROR);
        }
        return ResponseResult.success(user.getAccount());
    }


    @PostMapping("/payMoney")
    @ResponseBody
    public ResponseResult payAccount(@AuthenticationPrincipal User user,String money, HttpSession session){
        System.out.println(money);
        BigDecimal payMoney = new BigDecimal(money);
        BigDecimal account = user.getAccount();
        OrderInfo orderInfo = (OrderInfo)session.getAttribute("SESSION_LATEST_EXPRESS");
        if(orderInfo == null || money == null) {
            throw new CustomException(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }
        // 生成订单 & 订单支付
        ResponseResult result1 = orderInfoService.createOrder(orderInfo, payMoney,account, user.getId());
        if(result1.getCode() == ResponseErrorCodeEnum.SUCCESS.getCode()) {
            OrderPayment orderPayment = (OrderPayment) result1.getData();
            if (orderPayment.getPaymentStatus() == PaymentStatusEnum.WAIT_BUYER_PAY){
                return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
            }
            BigDecimal lastAccount = account.subtract(payMoney);
            user.setAccount(lastAccount);
            if (!userService.updateById(user)){
                System.out.println("更新用户余额失败");
                return ResponseResult.failure(ResponseErrorCodeEnum.OPERATION_ERROR);
            }
        }
        return result1;
    }

    /**
     * Description:管理员信息修改
     */
    @PostMapping("/changeInfo")
    @ResponseBody
    public ResponseResult changeAdminInfo(@AuthenticationPrincipal User user,String tel) {
        System.out.println("用户修改前的号码："+user.getTel());
        user.setTel(tel);
        if(!userService.updateById(user)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.OPERATION_ERROR);
        }
        System.out.println("用户修改后的号码："+user.getTel());
        return ResponseResult.success();
    }

    /**
     * 读取快递公司数据
     */
    @GetMapping("/company")
    @ResponseBody
    public ResponseResult listCompany() {
        List<DataCompany> list = dataCompanyService.listAllByCache();
        System.out.println(list.size());
        return ResponseResult.success(list);
    }



}
