package com.vlinkage.xunyee.api.login.controller;

import com.vlinkage.xunyee.entity.result.R;
import com.vlinkage.xunyee.api.login.service.LoginService;
import com.vlinkage.xunyee.config.weixin.WxMaProperties;
import com.vlinkage.xunyee.config.weixin.WxMpProperties;
import com.vlinkage.xunyee.entity.response.ResLoginSuccessApp;
import com.vlinkage.xunyee.entity.response.ResLoginSuccessMini;
import com.vlinkage.xunyee.entity.response.ResRefreshToken;
import com.vlinkage.xunyee.jwt.PassToken;
import com.vlinkage.xunyee.utils.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "登录注销")
@RequestMapping("auth")
@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;
    @Autowired
    private WxMpProperties wxMpProperties;
    @Autowired
    private WxMaProperties wxMaProperties;

    @ApiOperation(value="微信登录 app")
    @PassToken
    @PostMapping("login/wx/app")
    public R<ResLoginSuccessApp> appWxLogin(String code) {
        String appId=wxMpProperties.getConfigs().get(0).getAppid();
        return loginService.wxOpenLogin(appId,code,6);
    }


    @ApiOperation(value="微信登录 小程序")
    @PassToken
    @GetMapping("login/wx/miniprogram")
    public R<ResLoginSuccessMini> miniWxLogin(String code) {
        String appId=wxMaProperties.getConfigs().get(0).getAppid();

        return loginService.wxLoginMini(appId,code,5);
    }

    @ApiOperation(value="使用refresh_token刷新token",notes = "登录成功后都会返回token和refresh_token，token用于登录认证，" +
            "refresh_token用于刷新token，当本系统接口code返回30001的时候表示token和refresh_token都过期了，" +
            "需要重新登录（login/wx/app或login/wx/miniprogram），当code返回30002可以通过refresh_token调用此接口换取新的token避免" +
            "用户频繁登录")
    @PassToken
    @GetMapping("refresh/token")
    public R<ResRefreshToken> refreshToken(String refresh_token) {

        return loginService.refreshToken(refresh_token);
    }


    @ApiOperation(value="注销账号",notes = "用户注销账号，所有数据都是逻辑删除，首先他的账号会被标记is_enabled=false，" +
            "动态is_deleted=true，关联的东西都会被标记is_deleted=true")
    @PostMapping("close_account")
    public R closeAccount(HttpServletRequest request) {
        int userId= UserUtil.getUserId(request);
        return loginService.closeAccount(userId);
    }

}
