package com.vlinkage.xunyee.api.xunyee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.meta.service.MetaService;
import com.vlinkage.xunyee.api.xunyee.service.XunyeeService;
import com.vlinkage.xunyee.entity.ReqMyPage;
import com.vlinkage.xunyee.entity.request.*;
import com.vlinkage.xunyee.entity.response.*;
import com.vlinkage.xunyee.jwt.PassToken;
import com.vlinkage.xunyee.utils.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Api(tags = "寻艺基础接口")
@RequestMapping("xunyee")
@RestController
public class XunyeeController {

    @Autowired
    private XunyeeService xunyeeService;
    @Autowired
    private MetaService metaService;

    @ApiOperation("获取 封面、轮播图、广告")
    @PassToken
    @GetMapping("pic/current")
    public R<Map> getPic(@Valid ReqPic req){

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

    @ApiOperation("寻艺通知")
    @GetMapping("system/notification")
    public R<IPage<ResSystemNotification>> systemNotification(HttpServletRequest request, ReqMyPage myPage){
        int userId= UserUtil.getUserId(request);
        return xunyeeService.systemNotification(userId,myPage);
    }

    @ApiOperation("寻艺通知 标记已读")
    @GetMapping("system/notification/read")
    public R systemNotificationRead(int id){

        return xunyeeService.systemNotificationRead(id);
    }

    @ApiOperation("搜索艺人")
    @PassToken
    @GetMapping("person/search")
    public R<IPage<ResPerson>> personSearch(ReqMyPage myPage,String name){
        IPage<ResPerson> iPage=metaService.getPersonPage(myPage,name);
        return R.OK(iPage);
    }

    @ApiOperation("艺人相关的品牌")
    @PassToken
    @GetMapping("brand/person")
    public R<IPage<ResBrandPerson>> brandPerson(ReqMyPage myPage,int person_id){
        IPage<ResBrandPerson> iPage=metaService.getBrandPerson(myPage,person_id);
        return R.OK(iPage);
    }

    @ApiOperation("用户权益")
    @GetMapping("vcuser_benefit/current")
    public R vcuserBenefit(HttpServletRequest request){
        int userId= UserUtil.getUserId(request);
        return xunyeeService.vcuserBenefit(userId);
    }

    @PassToken
    @ApiOperation("签到榜")
    @GetMapping("person_check_count/rank")
    public R personCheckCountRank(HttpServletRequest request, ReqPersonCheckCount req){
        Integer userId= UserUtil.getUserId(request);
        return xunyeeService.personCheckCount(userId,req);
    }

    @ApiOperation("签到--我的爱豆")
    @GetMapping("person_check_count/idol")
    public R personCheckCountIdol(HttpServletRequest request,ReqMyPage myPage){
        int userId= UserUtil.getUserId(request);
        return xunyeeService.personCheckCountIdol(userId,myPage);
    }


    @ApiOperation("取消关注某个艺人")
    @GetMapping("vcuser_person")
    public R vcuserPerson(HttpServletRequest request, ReqUserPersonCheck req){
        int userId= UserUtil.getUserId(request);
        return xunyeeService.vcuserPerson(userId,req);
    }

    @PassToken
    @ApiOperation("权益价格")
    @GetMapping("benefit_price/current")
    public R<ResXunyeeBenefitPrice> benefitPrice(){

        return xunyeeService.benefitPrice();
    }


    @PassToken
    @ApiOperation("签到接口")
    @GetMapping("vcuser_person_check")
    public R vcuserPersonCHeck(HttpServletRequest request, ReqPersonCheck req){
//        int userId= UserUtil.getUserId(request);
        int userId= 23;
        return xunyeeService.vcuserPersonCHeck(userId,req);
    }

}
