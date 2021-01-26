package com.vlinkage.xunyee.api.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.user.service.UserService;
import com.vlinkage.xunyee.entity.request.ReqPageFollow;
import com.vlinkage.xunyee.entity.request.ReqUserInfo;
import com.vlinkage.xunyee.entity.response.ResFollowPage;
import com.vlinkage.xunyee.entity.response.ResSearchHot;
import com.vlinkage.xunyee.jwt.PassToken;
import com.vlinkage.xunyee.utils.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "用户模块")
@RequestMapping("vcuser")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation("查看个人资料")
    @GetMapping("info")
    public R getUser(HttpServletRequest request){
        int userId= UserUtil.getUserId(request);
        return userService.getUser(userId);
    }

    @ApiOperation("修改个人资料")
    @PutMapping("info")
    public R editUser(HttpServletRequest request, @Valid ReqUserInfo req){
        int userId= UserUtil.getUserId(request);
        return userService.editUser(userId,req);
    }

    @ApiOperation("关注/取消关注")
    @PostMapping("follow")
    public R follow(HttpServletRequest request,int vcuser_id){
        Integer from_userid=UserUtil.getUserId(request);
        return userService.follow(from_userid,vcuser_id);
    }

    @ApiOperation("我的关注/我的粉丝")
    @ApiImplicitParam(name = "type",value = "1 我的关注 2 我的粉丝")
    @GetMapping("follow")
    public R<IPage<ResFollowPage>> getFollows(HttpServletRequest request, ReqPageFollow req){
        Integer userId=UserUtil.getUserId(request);
        return userService.getFollows(userId,req);
    }
}
