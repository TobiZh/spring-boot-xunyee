package com.vlinkage.xunyee.api.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.user.service.UserService;
import com.vlinkage.xunyee.entity.request.ReqBlogReport;
import com.vlinkage.xunyee.entity.request.ReqPageFollow;
import com.vlinkage.xunyee.entity.request.ReqUserInfo;
import com.vlinkage.xunyee.entity.request.ReqVcuserId;
import com.vlinkage.xunyee.entity.response.ResFollowPage;
import com.vlinkage.xunyee.entity.response.ResMine;
import com.vlinkage.xunyee.jwt.PassToken;
import com.vlinkage.xunyee.utils.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Api(tags = "用户模块")
@RequestMapping("vcuser")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation("我的TAB")
    @GetMapping("mine")
    public R<ResMine> mine(HttpServletRequest request){
        int userId= UserUtil.getUserId(request);
        return userService.getUser(userId);
    }

    @ApiOperation("修改个人资料")
    @PutMapping("info")
    public R editUser(HttpServletRequest request, @Valid ReqUserInfo req){
        int userId= UserUtil.getUserId(request);
        return userService.editUser(userId,req);
    }

    @PassToken
    @ApiOperation("关注/取消关注")
    @PostMapping("follow")
    public R follow(HttpServletRequest request,@Valid ReqVcuserId req){
        int from_userid=UserUtil.getUserId(request);
        return userService.follow(from_userid,req.getVcuser_id());
    }

    @ApiOperation("我的关注/我的粉丝")
    @ApiImplicitParam(name = "type",value = "1 我的关注 2 我的粉丝")
    @GetMapping("follow")
    public R<IPage<ResFollowPage>> getFollows(HttpServletRequest request,@Valid ReqPageFollow req){
        Integer userId=UserUtil.getUserId(request);
        return userService.getFollows(userId,req);
    }

    @ApiOperation("举报用户")
    @PostMapping("report")
    public R report(HttpServletRequest request,@Valid ReqBlogReport req){
        Integer userId=UserUtil.getUserId(request);
        req.setVcuser_id(userId);
        return userService.report(req);
    }
}
