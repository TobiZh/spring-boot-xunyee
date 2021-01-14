package com.vlinkage.xunyee.handle;

import com.vlinkage.common.entity.result.code.ResultCode;
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