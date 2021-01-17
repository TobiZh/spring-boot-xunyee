package com.vlinkage.xunyee.api.user.controller;

import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.user.service.UserService;
import com.vlinkage.xunyee.entity.request.ReqAd;
import com.vlinkage.xunyee.entity.response.ResAd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "用户模块")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation("测试")
    @GetMapping("test")
    public R<List<ResAd>> test(@Valid ReqAd reqAd){

        return R.OK(reqAd);
    }

    @ApiOperation("查看个人资料")
    @GetMapping("vcuser/info/{id}")
    public R getUser(@PathVariable("id") int id){

        return userService.getUser(id);
    }
}
