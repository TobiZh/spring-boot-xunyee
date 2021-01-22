package com.vlinkage.xunyee.api.blog.controller;

import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.blog.service.BlogService;
import com.vlinkage.xunyee.entity.request.ReqBlog;
import com.vlinkage.xunyee.utils.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "创作动态相关")
@RequestMapping("blog")
@RestController
public class BlogController {

    @Autowired
    private BlogService blogService;


    @ApiModelProperty("发布动态")
    @PostMapping("edit")
    public R blog(HttpServletRequest request, ReqBlog req){
        int userId= UserUtil.getUserId(request);
        return blogService.blog(userId,req);
    }
}
