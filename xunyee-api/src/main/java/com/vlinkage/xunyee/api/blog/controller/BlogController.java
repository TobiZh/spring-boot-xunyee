package com.vlinkage.xunyee.api.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.blog.service.BlogService;
import com.vlinkage.xunyee.entity.ReqMyPage;
import com.vlinkage.xunyee.entity.request.ReqBlog;
import com.vlinkage.xunyee.entity.request.ReqPageBlogUser;
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
import javax.validation.constraints.NotNull;

@Api(tags = "动态相关")
@RequestMapping("blog")
@RestController
public class BlogController {

    @Autowired
    private BlogService blogService;


    @ApiOperation("发布动态")
    @PostMapping("edit")
    public R blog(HttpServletRequest request, ReqBlog req){
        int userId= UserUtil.getUserId(request);
        return blogService.blog(userId,req);
    }

    @ApiOperation("获取TA的动态")
    @GetMapping("user")
    public R<IPage<ResBlogPage>> getBlogByUserId(HttpServletRequest request,ReqPageBlogUser req){
        if (req.getVcuserId()==null){
            req.setVcuserId(UserUtil.getUserId(request));
        }
        return blogService.getBlogByUserId(req);
    }

    @ApiOperation("首页动态 剧作截图/现场热拍/品牌代言")
    @ApiImplicitParam(name = "type",value = "动态类型 1 截屏 2 我在现场 3 品牌代言")
    @PassToken
    @GetMapping("category")
    public R<IPage<ResBlogPage>> blogCategory(ReqMyPage myPage, Integer type){

        return blogService.blogCategory(myPage,type);
    }

    @ApiOperation("动态详情")
    @PassToken
    @GetMapping("info")
    public R<ResBlogInfo> blogInfo(HttpServletRequest request,int blogId){
        Integer userId=UserUtil.getUserId(request);
        return blogService.blogInfo(userId,blogId);
    }


    @ApiOperation("点赞（取消点赞）/点踩（取消点踩）")
    @GetMapping("star")
    public R blogStar(HttpServletRequest request,int blogId,int type){
        Integer userId=UserUtil.getUserId(request);
        return blogService.blogStar(userId,blogId,type);
    }

    @ApiOperation("收藏/取消收藏")
    @GetMapping("favorite")
    public R blogFavorite(HttpServletRequest request,int blogId){
        Integer userId=UserUtil.getUserId(request);
        return blogService.blogFavorite(userId,blogId);
    }
}
