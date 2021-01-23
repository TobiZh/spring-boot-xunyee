package com.vlinkage.xunyee.utils;


import javax.servlet.http.HttpServletRequest;

/**
 * 用户工具类
 */
public class UserUtil {

    /**
     * 用户登录成功后 通过interceptor讲userId从jwt中取出存入HttpServletRequest
     * @param request
     * @return
     */
    public static Integer getUserId(HttpServletRequest request){
        Integer userId= Integer.parseInt(request.getAttribute("userId").toString());
        return userId;
    }
}
