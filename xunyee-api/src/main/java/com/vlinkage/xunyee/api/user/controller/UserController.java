package com.vlinkage.xunyee.api.user.controller;

import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.user.service.UserService;
import com.vlinkage.xunyee.entity.request.ReqUserInfo;
import com.vlinkage.xunyee.utils.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Api(tags = "用户模块")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation("查看个人资料")
    @GetMapping("vcuser/info")
    public R getUser(HttpServletRequest request){
        int userId= UserUtil.getUserId(request);
        return userService.getUser(userId);
    }

    @ApiOperation("修改个人资料")
    @PutMapping("vcuser/info")
    public R editUser(HttpServletRequest request, @Valid ReqUserInfo req){
        int userId= UserUtil.getUserId(request);
        return userService.editUser(userId,req);
    }

}
