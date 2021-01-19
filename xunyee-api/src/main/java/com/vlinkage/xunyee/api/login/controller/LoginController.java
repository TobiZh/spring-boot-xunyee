package com.vlinkage.xunyee.api.login.controller;

import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.login.service.LoginService;
import com.vlinkage.xunyee.config.weixin.WxMpProperties;
import com.vlinkage.xunyee.jwt.PassToken;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@CrossOrigin(maxAge = 3600)
@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;
    @Autowired
    private WxMpProperties wxMpProperties;


    @ApiOperation(value="app微信登录")
    @PassToken
    @PostMapping("third/part/login")
    public R appWxLogin(@Valid ThirdLoginParam loginParam) {
        String appId=wxMpProperties.getConfigs().get(0).getAppId();
        return loginService.wxOpenLogin(appId,loginParam);
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
    public R h5Login(@Valid ThirdLoginParam loginParam) {
        String appId=wxMpProperties.getConfigs().get(1).getAppId();
        return loginService.wxOpenLogin(appId,loginParam);
    }
}
