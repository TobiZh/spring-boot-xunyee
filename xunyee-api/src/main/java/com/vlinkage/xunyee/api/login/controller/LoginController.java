package com.vlinkage.xunyee.api.login.controller;

import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.login.service.LoginService;
import com.vlinkage.xunyee.config.weixin.WxMaProperties;
import com.vlinkage.xunyee.config.weixin.WxMpProperties;
import com.vlinkage.xunyee.jwt.PassToken;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@CrossOrigin(maxAge = 3600)
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
    @PostMapping("login/wx/app/{code}")
    public R appWxLogin(@NotNull(message = "code不能为空") @PathVariable("code") String code) {
        String appId=wxMpProperties.getConfigs().get(0).getAppId();
        return loginService.wxOpenLogin(appId,code);
    }

    @ApiOperation(value="微信登录 小程序")
    @PassToken
    @PostMapping("login/wx/miniprogram/{code}")
    public R miniWxLogin(@NotNull(message = "code不能为空") @PathVariable("code") String code,
                         String signature, String rawData, String encryptedData, String iv) {
        String appId=wxMaProperties.getConfigs().get(0).getAppId();

        return loginService.wxOpenLoginMini(appId,code,signature,rawData,encryptedData,iv);
    }


    @ApiOperation(value="jsapi获取openid")
    @PassToken
    @GetMapping("h5/openid")
    public R getOpenId(String code) {
        String appId=wxMpProperties.getConfigs().get(1).getAppId();

        return loginService.getOpenId(appId,code);
    }

    @ApiOperation(value="h5微信登录前用户授权")
    @PassToken
    @GetMapping("h5/authorization/redirectUri")
    public R buildAuthorizationUrl(String redirectUri,String scope) {
        String appId=wxMpProperties.getConfigs().get(1).getAppId();

        return loginService.buildAuthorizationUrl(appId,redirectUri,scope);
    }

    @ApiOperation(value="h5微信登录")
    @PassToken
    @PostMapping("h5/login")
    public R h5Login(@NotNull(message = "code不能为空") String code) {
        String appId=wxMpProperties.getConfigs().get(1).getAppId();
        return loginService.wxOpenLogin(appId,code);
    }
}
