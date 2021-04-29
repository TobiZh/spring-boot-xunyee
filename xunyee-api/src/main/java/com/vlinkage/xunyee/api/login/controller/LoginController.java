package com.vlinkage.xunyee.api.login.controller;

import com.vlinkage.xunyee.entity.result.R;
import com.vlinkage.xunyee.api.login.service.LoginService;
import com.vlinkage.xunyee.config.weixin.WxMaProperties;
import com.vlinkage.xunyee.config.weixin.WxMpProperties;
import com.vlinkage.xunyee.entity.response.ResLoginSuccessApp;
import com.vlinkage.xunyee.entity.response.ResLoginSuccessMini;
import com.vlinkage.xunyee.entity.response.ResRefreshToken;
import com.vlinkage.xunyee.jwt.PassToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "登录注销")
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


    @ApiOperation(value="使用refresh_token刷新token")
    @PassToken
    @GetMapping("refresh/token")
    public R<ResRefreshToken> refreshToken(String refresh_token) {

        return loginService.refreshToken(refresh_token);
    }


}
