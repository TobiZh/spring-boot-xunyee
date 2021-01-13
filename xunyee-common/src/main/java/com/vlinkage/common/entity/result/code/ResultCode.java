package com.vlinkage.common.entity.result.code;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(0,"操作成功"),
    ERROR(-1,"操作失败"),
    VALIDATE_FAILED(1002, "参数校验失败"),
    TOKEN_ERROR(4001, "无token，请重新登录"),
    NOT_TOKEN_RE_LOGIN(4001, "token验证失败，请重新登录"),
    NOT_TOKEN_USER_LOGIN(4001, "登录已失效，请重新登录"),
    ;

    private int code;
    private String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
