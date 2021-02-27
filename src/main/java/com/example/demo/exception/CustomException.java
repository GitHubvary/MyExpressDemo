package com.example.demo.exception;

import com.example.demo.domain.ResponseResult;
import com.example.demo.domain.enums.ResponseErrorCodeEnum;

/**
 * Description:
 * date: 2021/2/25 10:34
 */
public class CustomException extends RuntimeException {
    private Integer code;

    public CustomException(ResponseErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum.getMsg());
        this.code = errorCodeEnum.getCode();
    }

    public CustomException(ResponseResult result) {
        super(result.getMsg());
        this.code = result.getCode();
    }

    public CustomException(Integer code , String info) {
        super(info);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
