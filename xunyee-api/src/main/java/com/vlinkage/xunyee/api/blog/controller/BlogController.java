package com.vlinkage.xunyee.api.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vlinkage.xunyee.entity.response.ResBlogStar;
import com.vlinkage.xunyee.entity.result.R;
import com.vlinkage.xunyee.entity.result.code.ResultCode;
import com.vlinkage.xunyee.api.blog.service.BlogService;
import com.vlinkage.xunyee.entity.ReqMyPage;
import com.vlinkage.xunyee.entity.request.*;
import com.vlinkage.xunyee.entity.response.ResBlogInfo;
import com.vlinkage.xunyee.entity.response.ResBlogPage;
import com.vlinkage.xunyee.jwt.PassToken;
import com.vlinkage.xunyee.utils.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Api(tags = "动态相关")
@RequestMapping("blog")
@RestController
public class BlogController {

    @Autowired
    private BlogService blogService;


    @ApiOperation("发布动态")
    @PostMapping("edit")
    public R blog(HttpServletRequest request,@Valid ReqBlog req){
        Integer userId= UserUtil.getUserId(request);
        return blogService.blog(userId,req);
    }

    @ApiOperation("首页动态关注、推荐、截屏、现场热拍、品牌代言")
    @PassToken
    @GetMapping("category")
    public R<IPage<ResBlogPage>> blogCategory(HttpServletRequest request, ReqMyPage myPage, @Valid ReqBlogCategory req){
        Integer userId=UserUtil.getUserId(request);
        int type= req.getType();
        if (type==0){//关注
            if (userId==null){
                return R.ERROR(ResultCode.NO_TOKEN_TO_LOGIN);
            }
            return blogService.blogFollow(myPage,userId);
        }else if (type==1){//推荐
            return blogService.blogCategory(myPage,type,userId);
        }else{//截屏、现场热拍、品牌代言
            return blogService.blogCategory(myPage,type-1,userId);
        }
    }

    @ApiOperation("动态详情")
    @PassToken
    @GetMapping("info")
    public R<ResBlogInfo> blogInfo(HttpServletRequest request,@Valid ReqBlogId req){
        Integer userId=UserUtil.getUserId(request);
        return blogService.blogInfo(userId,req.getBlog_id());
    }

    @ApiOperation("推荐动态")
    @PassToken
    @GetMapping("recommend")
    public R<IPage<ResBlogPage>> recommend(HttpServletRequest request,ReqMyPage myPage,@Valid ReqRecommendPage req){
        Integer userId=UserUtil.getUserId(request);

        return blogService.recommend(myPage,req,userId);
    }


    @ApiOperation("点赞（取消点赞）/点踩（取消点踩）")
    @PostMapping("star")
    public R<ResBlogStar> blogStar(HttpServletRequest request, @Valid ReqBlogStar req){
        Integer userId=UserUtil.getUserId(request);
        return blogService.blogStar(userId,req);
    }

    @ApiOperation("收藏/取消收藏")
    @PostMapping("favorite")
    public R blogFavorite(HttpServletRequest request,@Valid ReqBlogId req){
        Integer userId=UserUtil.getUserId(request);
        return blogService.blogFavorite(userId,req.getBlog_id());
    }


    @ApiOperation("举报动态")
    @PostMapping("report")
    public R blogReport(HttpServletRequest request, @Valid ReqBlogReport req){
        int userId=UserUtil.getUserId(request);
        return blogService.blogReport(userId,req);
    }

    @ApiOperation("TA的动态")
    @PassToken
    @GetMapping("vcuser")
    public R<IPage<ResBlogPage>> getBlogByUserId(HttpServletRequest request,@Valid ReqPageBlogUser req){
        Integer userId=UserUtil.getUserId(request);
        return blogService.getBlogByUserId(userId,req);
    }

    @ApiOperation("我的发布")
    @GetMapping("mine")
    public R<IPage<ResBlogPage>> getMineBlog(HttpServletRequest request,ReqMyPage myPage,String name){
        int userId=UserUtil.getUserId(request);
        return blogService.getMineBlog(myPage,userId,name);
    }

    @ApiOperation("好友的动态")
    @GetMapping("friend")
    public R<IPage<ResBlogPage>> getBlogByFriend(HttpServletRequest request,ReqMyPage myPage){
        int userId=UserUtil.getUserId(request);
        return blogService.getBlogByFriend(myPage,userId);
    }

    @ApiOperation("删除动态")
    @GetMapping("del")
    public R delBlog(HttpServletRequest request,@Valid ReqBlogId req){
        Integer userId=UserUtil.getUserId(request);
        return blogService.delBlog(userId,req.getBlog_id());
    }
}
