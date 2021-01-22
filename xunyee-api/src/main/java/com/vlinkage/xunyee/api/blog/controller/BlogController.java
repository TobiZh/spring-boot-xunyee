package com.vlinkage.xunyee.api.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.blog.service.BlogService;
import com.vlinkage.xunyee.entity.ReqMyPage;
import com.vlinkage.xunyee.entity.request.ReqBlog;
import com.vlinkage.xunyee.entity.request.ReqPageBlogUser;
import com.vlinkage.xunyee.entity.response.ResBlogPage;
import com.vlinkage.xunyee.utils.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "创作动态相关")
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
        if (req.getVcuser_id()==null){
            req.setVcuser_id(UserUtil.getUserId(request));
        }
        return blogService.getBlogByUserId(req);
    }
}
