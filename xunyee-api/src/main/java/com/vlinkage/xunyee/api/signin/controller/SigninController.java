package com.vlinkage.xunyee.api.signin.controller;

import com.vlinkage.xunyee.api.signin.service.SigninService;
import com.vlinkage.xunyee.entity.request.ReqPersonId;
import com.vlinkage.xunyee.entity.result.R;
import com.vlinkage.xunyee.utils.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Api(tags = "签到")
@RequestMapping("signin")
@RestController
public class SigninController {

    @Autowired
    private SigninService signinService;


    @ApiOperation(value = "签到之前先获调用这个验证一下",notes = "通过 code 来控制弹窗，" +
            "code = 0 弹 去签到，data返回一个对象；前端通过判断 data是否为空来控制显示隐藏，" +
            "data为空的时候隐藏广告链接；data不为空的时候显示广告链接，" +
            "code = -1 弹 toast" +
            "code = 30001 弹 登录，" +
            "code = 20004 弹 开通会员...")
    @GetMapping("vcuser_person_check/verify")
    public R vcuserPersonCheckVerify(HttpServletRequest request,@Valid ReqPersonId req){
        int userId= UserUtil.getUserId(request);
        return signinService.vcuserPersonCheckVerify(userId,req.getPerson());
    }

    @ApiOperation("签到")
    @PostMapping("vcuser_person_check")
    public R vcuserPersonCheck(HttpServletRequest request, @Valid ReqPersonId req){
        int userId= UserUtil.getUserId(request);
        return signinService.vcuserPersonCheck(userId,req.getPerson());
    }
}
