package com.vlinkage.common.entity.result;

import com.vlinkage.common.entity.result.code.ResultCode;
import lombok.Data;

@Data
public class R<T> {

    private int code;
    private String msg;
    private T data;



    /**
     * 返回成功信息
     *
     * @param object 传入的数据
     * @return Result
     */
    public static R OK(Object object) {
        R result = new R();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        result.setData(object);
        return result;
    }

    /**
     * 返回失败信息
     *
     * @param code 状态码
     * @param msg  信息
     * @return Result
     */
    public static R ERROR(Integer code, String msg) {
        R result = new R();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public static R ERROR(Integer code, String msg,Object object) {
        R result = new R();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(object);
        return result;
    }

    /**
     * 返回成功信息
     *
     * @return AjaxMsg
     */
    public static R OK() {
        return R.OK(null);
    }


    public static R ERROR() {
        return R.ERROR(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMsg());
    }
    public static R ERROR(String msg) {
        return R.ERROR(ResultCode.ERROR.getCode(), msg);
    }
    public static R ERROR(ResultCode resultCode) {
        return R.ERROR(resultCode.getCode(), resultCode.getMsg());
    }
    public static R ERROR(ResultCode resultCode,Object object) {
        return R.ERROR(resultCode.getCode(), resultCode.getMsg(),object);
    }


}
