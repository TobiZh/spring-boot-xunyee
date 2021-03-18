package com.vlinkage.xunyee.api.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.blog.service.BlogService;
import com.vlinkage.xunyee.entity.ReqMyPage;
import com.vlinkage.xunyee.entity.request.*;
import com.vlinkage.xunyee.entity.response.ResBlogInfo;
import com.vlinkage.xunyee.entity.response.ResBlogPage;
import com.vlinkage.xunyee.jwt.PassToken;
import com.vlinkage.xunyee.utils.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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

    @ApiOperation("首页动态 关注")
    @GetMapping("follow")
    public R<IPage<ResBlogPage>> blogFollow(HttpServletRequest request,ReqMyPage myPage){
        int userId=UserUtil.getUserId(request);
        return blogService.blogFollow(myPage,userId);
    }

    @ApiOperation("首页动态 剧作截图/现场热拍/品牌代言")
    @PassToken
    @GetMapping("category")
    public R<IPage<ResBlogPage>> blogCategory(HttpServletRequest request, ReqMyPage myPage, @Valid ReqBlogCategory req){
        Integer userId=UserUtil.getUserId(request);
        return blogService.blogCategory(myPage,req.getType(),userId);
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
    public R blogStar(HttpServletRequest request,int blog_id,int type){
        Integer userId=UserUtil.getUserId(request);
        return blogService.blogStar(userId,blog_id,type);
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
        Integer userId=UserUtil.getUserId(request);
        req.setVcuser_id(userId);
        return blogService.blogReport(req);
    }

    @ApiOperation("获取TA的动态")
    @GetMapping("vcuser")
    public R<IPage<ResBlogPage>> getBlogByUserId(HttpServletRequest request,ReqPageBlogUser req){
        if (req.getVcuser_id()==null){
            req.setVcuser_id(UserUtil.getUserId(request));
        }
        return blogService.getBlogByUserId(req);
    }


    @ApiOperation("好友的动态")
    @GetMapping("friend")
    public R<IPage<ResBlogPage>> getBlogByFriend(HttpServletRequest request){
        int userId=UserUtil.getUserId(request);
        return blogService.getBlogByFriend(userId);
    }
}
