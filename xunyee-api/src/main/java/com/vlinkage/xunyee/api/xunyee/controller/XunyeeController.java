package com.vlinkage.xunyee.api.xunyee.controller;

import com.vlinkage.ant.xunyee.entity.XunyeeFeedback;
import com.vlinkage.ant.xunyee.entity.XunyeeNavigation;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.xunyee.service.XunyeeService;
import com.vlinkage.xunyee.entity.request.ReqFeedback;
import com.vlinkage.xunyee.entity.request.ReqPic;
import com.vlinkage.xunyee.entity.response.ResNavigation;
import com.vlinkage.xunyee.entity.response.ResPic;
import com.vlinkage.xunyee.entity.response.ResSearchHot;
import com.vlinkage.xunyee.jwt.PassToken;
import com.vlinkage.xunyee.utils.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "寻艺基础接口")
@RestController
public class XunyeeController {

    @Autowired
    private XunyeeService xunyeeService;


    @ApiOperation("获取 封面、轮播图、广告")
    @PassToken
    @GetMapping("pic/current")
    public R<List<ResPic>> getPic(@Valid ReqPic req){

        return xunyeeService.getPic(req);
    }

    @ApiOperation("快速导航按钮")
    @PassToken
    @GetMapping("navigation")
    public R<List<ResNavigation>> getNavigation(){

        return xunyeeService.getNavigation();
    }

    @ApiOperation("热门搜索")
    @PassToken
    @GetMapping("search/hot")
    public R<List<ResSearchHot>> getSearchHot(){

        return xunyeeService.getSearchHot();
    }

    @ApiOperation("意见反馈")
    @GetMapping("feedback")
    public R feedback(HttpServletRequest request, ReqFeedback req){
        int userId= UserUtil.getUserId(request);
        return xunyeeService.feedback(userId,req);
    }
}
