package com.vlinkage.xunyee.api.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vlinkage.xunyee.entity.result.R;
import com.vlinkage.xunyee.api.user.service.UserService;
import com.vlinkage.xunyee.entity.ReqMyPage;
import com.vlinkage.xunyee.entity.request.*;
import com.vlinkage.xunyee.entity.response.*;
import com.vlinkage.xunyee.jwt.PassToken;
import com.vlinkage.xunyee.utils.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@Api(tags = "用户模块")
@RequestMapping("vcuser")
@RestController
public class UserController {

    @Autowired
    private UserService userService;


    @ApiOperation("用户详情本人和非本人")
    @PassToken
    @GetMapping("")
    public R<ResUserInfoOhter> other(HttpServletRequest request, ReqVcuserId req){
        Integer userId= UserUtil.getUserId(request);
        return userService.other(userId,req.getVcuser_id());
    }

    @ApiOperation("我的TAB 头像、点赞、关注、我的爱豆数量")
    @GetMapping("mine")
    public R<ResMine> mine(HttpServletRequest request){
        int userId= UserUtil.getUserId(request);
        return userService.getUser(userId);
    }

    @ApiOperation("修改个人资料")
    @PutMapping("info")
    public R editUser(HttpServletRequest request, ReqUserInfo req){
        int userId= UserUtil.getUserId(request);
        return userService.editUser(userId,req);
    }


    @ApiOperation("关注/取消关注")
    @PostMapping("follow")
    public R follow(HttpServletRequest request,@Valid ReqVcuserId req){
        int from_userid=UserUtil.getUserId(request);
        return userService.follow(from_userid,req.getVcuser_id());
    }

    @ApiOperation("我的关注/我的粉丝")
    @GetMapping("follow")
    public R<IPage<ResFollowPage>> getFollows(HttpServletRequest request,@Valid ReqPageFollow req){
        Integer userId=UserUtil.getUserId(request);
        return userService.getFollows(userId,req);
    }

    @ApiOperation("举报用户")
    @PostMapping("report")
    public R report(HttpServletRequest request,@Valid ReqUserReport req){
        int userId=UserUtil.getUserId(request);
        return userService.report(userId,req);
    }

    @ApiOperation("我关注的艺人")
    @GetMapping("person")
    public R<IPage<ResPerson>> vcuserPerson(HttpServletRequest request, ReqMyPage myPage){
        int userId= UserUtil.getUserId(request);
        return userService.vcuserPerson(userId,myPage);
    }


    @ApiOperation("我的获赞")
    @GetMapping("blog/star")
    public R<IPage<ResBlogStarPage>> getBlogStar(HttpServletRequest request,@Valid ReqMyPage myPage){
        int userId=UserUtil.getUserId(request);
        return userService.getBlogStar(userId,myPage);
    }

    @ApiOperation("上传封面图")
    @PostMapping("cover")
    public R uploadCover(HttpServletRequest request,@RequestParam("file") MultipartFile file) throws IOException {
        int userId=UserUtil.getUserId(request);
        return userService.uploadCover(userId,file);
    }


    @ApiOperation("默认封面图")
    @PostMapping("cover/default")
    public R uploadCoverDefault(HttpServletRequest request) {
        int userId=UserUtil.getUserId(request);
        return userService.uploaduploadCoverDefaultCover(userId);
    }

    @ApiOperation("我赞过/收藏/浏览")
    @GetMapping("blog/star_favorite_brow")
    public R blogStarFavoriteBrow(HttpServletRequest request,@Valid ReqMyPage myPage,int type) {
        int userId=UserUtil.getUserId(request);
        return userService.blogStarFavoriteBrow(userId,myPage,type);
    }

}
