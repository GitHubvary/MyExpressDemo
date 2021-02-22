package com.example.demo.domain.enums;


import lombok.Getter;

@Getter
public enum ResponseErrorCodeEnum {
    /* SUCCESS */
    SUCCESS(0, 200, "成功"),

    /* Bad Request */
    PARAMETER_ERROR(400001, 400, "参数错误"),
    SYSTEM_ERROR(400002, 400, "系统错误"),
    OPERATION_ERROR(400003, 400, "操作失败"),
    VERIFY_CODE_ERROR(400004, 400, "验证码输入错误"),
    SCHOOL_NOT_EXIST(400005, 400, "高校不存在"),
    REAL_NAME_INVALID(400006, 400, "姓名中含有非法字符"),
    ID_CARD_INVALID(400007, 400, "身份证号不合法"),
    STUDENT_IDCARD_NOT_NUMBER(400008, 400, "学号必须为纯数字"),
    REGISTRY_ERROR(400009, 400, "注册失败"),
    UPLOAD_FILE_NOT_EXIST(400010, 400, "上传文件不存在"),
    MUST_NUMBER(400011, 400, "必须为数字"),
    MUST_POSITIVE_INTEGER(400012, 400, "必须为正整数"),
    REDIS_ERROR(400013,  400, "程序环境Redis出现故障，请提交反馈"),

    TEL_INVALID(400101, 400, "手机号码不合法"),
    SMS_SEND_INTERVAL_TOO_SHORT(400102, 400, "短信发送间隔不足%s分钟"),
    SMS_CODE_NOT_EXIST(400103, 400, "请先获取短信验证码"),
    SMS_EXPIRE(400104, 400, "短信验证码已过期，请重新发送"),
    SEND_SMS_ERROR(400105, 400, "发送短信失败"),
    SMS_TEL_NOT_MATCH(400106, 400, "申请的手机号码与登录手机号码不匹配"),

    PASSWORD_RESET_ERROR(400201, 400, "密码重置失败"),
    PASSWORD_IS_EMPTY(400202, 400, "密码不能为空"),
    PASSWORD_ERROR(400203, 400, "密码输入错误，请检查密码"),

    ORDER_NOT_OPEN_EVALUATE(400601, 400, "订单未开启评价"),
    ORDER_EVALUATE_ERROR(400602, 400, "订单评价失败"),
    OPEN_EVALUATE_ERROR(400603, 400, "开启订单评价失败"),
    CLOSE_EVALUATE_ERROR(400604, 400, "关闭开启评价失败"),
    ORDER_ALREADY_EVALUATE(400605, 400, "您已评价过该订单，请不要重复评价"),
    EVALUATE_SCORE_ERROR(400606, 400, "评分错误，评分为0~10分"),
    SCORE_UPDATE_ERROR(400607, 400, "个人评分更新失败"),

    ORDER_CREATE_ERROR(400701, 400, "订单创建失败"),
    ORDER_PAYMENT_CREATE_ERROR(400702, 400, "订单支付信息创建失败"),
    ORDER_PAYMENT_SYNC_ERROR(400703, 400, "订单支付状态同步失败"),

    FEEDBACK_TYPE_ERROR(400801, 400, "反馈类型错误"),
    FEEDBACK_NOT_EMPTY(400802, 400, "反馈内容不能为空"),
    STR_LENGTH_OVER(400803, 400, "%s内容过长，请控制在%s字符内"),

    /* Unauthorized */
    LOGIN_ERROR(401001, 401, "登陆失败"),
    SESSION_EXPIRE(401003, 401, "Session已过期，请重新登录"),
    ACCOUNT_LOCKED(401004, 401, "账户已冻结，解冻时间为%s"),
    ACCOUNT_DISABLE(401005, 401, "账户失效，如有疑问，请提交反馈"),
    ACCOUNT_EXPIRE(401006, 401, "账户过期，如有疑问，请提交反馈"),
    PASSWORD_EXPIRE(401007, 401, "密码已过期"),

    /* Forbidden */
    ROLE_ERROR(403001, 403, "角色错误"),
    OPERATION_NOT_SUPPORT(403002, 403, "操作不支持"),
    NO_PERMISSION(403003, 403, "您没有权限操作"),

    /* Too Many Requests */
    REQUEST_TOO_HIGH(429001, 429, "接口请求过于频繁"),

    /* Conflict */
    USER_NOT_EXIST(409001, 409, "用户不存在"),
    COURIER_NOT_EXIST(409002, 409, "配送员不存在"),
    USERNAME_EXIST(409003, 409,"用户名已被注册，请更换其他用户名"),
    USERNAME_DISABLE_MODIFY(409004, 409, "用户名不支持修改"),
    IDCARD_EXIST(409005, 409,"该身份证已被注册，如有疑问请提交反馈"),

    IDCARD_OR_REALNAME_EXIST(409101, 409, "您已绑定实名信息"),
    TEL_EXIST(409102, 409, "该手机号已被注册，请更换其他手机号"),
    TEL_NOT_EXIST(409103, 409, "该手机号码尚未注册，请先注册再登录"),

    NOT_APPLY_REAL_NAME(409201, 409, "未完成实名认证"),

    ORDER_NOT_EXIST(409701, 409, "订单不存在"),
    EXIST_UNFINISHED_ORDER(409702, 409, "当前存在未完成的订单"),
    ;

    /**
     * EXPRESS ERROR CODE
     */
    private int code;
    /**
     * HTTP STATUS
     */
    private int status;

    private String msg;

    ResponseErrorCodeEnum(int code, int status, String msg) {
        this.code = code;
        this.status = status;
        this.msg = msg;
    }
}
