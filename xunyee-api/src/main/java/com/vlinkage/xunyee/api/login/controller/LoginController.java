package com.vlinkage.xunyee.api.login.controller;

import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.login.service.LoginService;
import com.vlinkage.xunyee.config.weixin.WxMaProperties;
import com.vlinkage.xunyee.config.weixin.WxMpProperties;
import com.vlinkage.xunyee.jwt.PassToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.constraints.NotNull;

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
    public R appWxLogin(@NotNull(message = "code不能为空")String code) {
        String appId=wxMpProperties.getConfigs().get(0).getAppid();
        return loginService.wxOpenLogin(appId,code);
    }

    @ApiOperation(value="微信登录 小程序")
    @PassToken
    @GetMapping("login/wx/miniprogram")
    public R miniWxLogin(String code) {
        String appId=wxMaProperties.getConfigs().get(0).getAppid();

        return loginService.wxLoginMini(appId,code);
    }
}
