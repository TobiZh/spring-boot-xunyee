package com.vlinkage.common.entity.result.code;

import lombok.Getter;

/**
 * 状态码
 */
@Getter
public enum ResultCode {
    SUCCESS(0,"操作成功"),
    ERROR(-1,"操作失败"),

    /* 系统500错误*/
    SYSTEM_ERROR(10000, "系统异常，请稍后重试"),


    /* 参数错误：10001-19999 */
    PARAM_IS_INVALID(10001, "参数无效"),


    /* 用户错误：20001-29999*/
    USER_HAS_EXISTED(20001, "用户已存在"),
    USER_LOGIN_FAIL(20002,"账号或密码错误"),
    USER_HAS_EXIST(20003,"账号已存在"),
    USER_NOT_EXIST(20003,"用户不存在，请重新登录"),

    /* 认证失败错误：30001-39999*/
    NO_TOKEN(30001,"无token，请重新登录"),
    TOKEN_OUT_TIME(30002,"token超时,请重新登录"),
    TOKEN_ILLEGAL(30003,"token 认证失败"),

    /* 文件失败错误：40001-49999*/
    EXCEL_NO_SHEET(40001,"Excel无Sheet")
    ;

    private int code;
    private String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
