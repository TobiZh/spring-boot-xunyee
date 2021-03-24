package com.vlinkage.xunyee.handle;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.vlinkage.ant.xunyee.entity.XunyeeVcuser;
import com.vlinkage.common.entity.result.code.ResultCode;
import com.vlinkage.common.redis.RedisUtil;
import com.vlinkage.xunyee.exception.BusinessException;
import com.vlinkage.xunyee.jwt.JwtUtil;
import com.vlinkage.xunyee.jwt.PassToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 自定义token拦截器
 * 没有 @PassToken 注解的接口 请求头中需要添加 Authorization
 */
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) throws Exception {
        String token = httpServletRequest.getHeader("token");// 从 http 请求头中取出 token
        // 如果不是映射到方法直接通过
        if (!(object instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) object;
        Method method = handlerMethod.getMethod();

        //检查是否有passtoken注释，有则跳过认证
        // 其它都需要登录认证
        if (method.isAnnotationPresent(PassToken.class)) {
            PassToken passToken = method.getAnnotation(PassToken.class);
            if (passToken.required()) {
                if(token!=null){
                    // 获取 token 中的 user id
                    String userId;
                    try {
                        userId = JwtUtil.getUserId(token);
                        if (StringUtils.isNotBlank(userId)){
                            // 将userId写入request
                            httpServletRequest.setAttribute("userId", userId);
                        }

                    } catch (JWTDecodeException j) {
                        throw new BusinessException(ResultCode.NO_TOKEN_TO_LOGIN);
                    }
                }
                return true;
            }
        }

        //检查有没有需要用户权限的注解

        // 执行认证
        if (token == null) {
            throw new BusinessException(ResultCode.NO_TOKEN_TO_LOGIN);
        }

        // 验证 token
        if (!JwtUtil.verify(token)) { // 通知用户去调用刷新token接口
            throw new BusinessException(ResultCode.TOKEN_OUT_TIME_TO_REFRESH);
        }

        // 获取 token 中的 user id
        String userId;
        try {
            userId = JwtUtil.getUserId(token);
        } catch (JWTDecodeException j) {
            throw new BusinessException(ResultCode.NO_TOKEN_TO_LOGIN);
        }

        if (!redisUtil.hasKey("user_toke:"+userId+":access_token")) {
            throw new BusinessException(ResultCode.NO_TOKEN_TO_LOGIN);
        }

        // 将userId写入request
        httpServletRequest.setAttribute("userId", userId);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse,
                           Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Object o, Exception e) throws Exception {
    }
}
