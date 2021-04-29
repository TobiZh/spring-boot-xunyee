package com.vlinkage.xunyee.exception;

import com.vlinkage.xunyee.entity.result.code.ResultCode;
import lombok.Data;

/**
 * 自定义异常
 */
@Data
public class BusinessException extends RuntimeException{
 
    private int code;

    private String msg;
 
 
    public BusinessException(ResultCode resultCode) {
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
    }
 
 
}