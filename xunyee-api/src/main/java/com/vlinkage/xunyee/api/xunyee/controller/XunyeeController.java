package com.vlinkage.xunyee.api.xunyee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.meta.MetaService;
import com.vlinkage.xunyee.api.xunyee.service.XunyeeService;
import com.vlinkage.xunyee.entity.ReqMyPage;
import com.vlinkage.xunyee.entity.request.*;
import com.vlinkage.xunyee.entity.response.*;
import com.vlinkage.xunyee.jwt.PassToken;
import com.vlinkage.xunyee.utils.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @ApiOperation("启动广告（不是闪屏页）")
    @PassToken
    @GetMapping("ad/launch")
    public R<ResPic> getAdLaunch(@Valid ReqPic req){

        return xunyeeService.getAdLaunch(req);
    }


    @ApiOperation("轮播图")
    @PassToken
    @GetMapping("ad/banner")
    public R<List<ResPic>> getAdBanner(@Valid ReqPic req){

        return xunyeeService.getAdBanner(req);
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
    @PostMapping("feedback")
    public R feedback(HttpServletRequest request,@Valid ReqFeedback req){
        int userId= UserUtil.getUserId(request);
        return xunyeeService.feedback(userId,req);
    }

    @ApiOperation("寻艺通知")
    @GetMapping("system/notification")
    public R<IPage<ResSystemNotification>> systemNotification(HttpServletRequest request, ReqMyPage myPage){
        int userId= UserUtil.getUserId(request);
        return xunyeeService.systemNotification(userId,myPage);
    }

    @ApiOperation("寻艺通知数量")
    @GetMapping("system/notification/count")
    public R<Integer> systemNotificationCount(HttpServletRequest request){
        int userId= UserUtil.getUserId(request);
        return xunyeeService.systemNotificationCount(userId);
    }

    @ApiOperation("寻艺通知 标记已读")
    @PutMapping("system/notification/read")
    public R systemNotificationRead(HttpServletRequest request,int id){
        int userId= UserUtil.getUserId(request);
        return xunyeeService.systemNotificationRead(userId,id);
    }

    @ApiOperation("寻艺通知 全部标记已读")
    @PutMapping("system/notification/read_all")
    public R systemNotificationReadAll(HttpServletRequest request){
        int userId= UserUtil.getUserId(request);
        return xunyeeService.systemNotificationReadALl(userId);
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
    public R<IPage<ResBrandPerson>> brandPerson(ReqMyPage myPage,Integer person_id){
        IPage<ResBrandPerson> iPage=metaService.getBrandPerson(myPage,person_id);
        return R.OK(iPage);
    }

    @ApiOperation("用户权益")
    @GetMapping("vcuser_benefit/current")
    public R vcuserBenefit(HttpServletRequest request){
        int userId= UserUtil.getUserId(request);
        return xunyeeService.vcuserBenefit(userId);
    }

    @ApiOperation("共有多少用户拥有权益")
    @PassToken
    @GetMapping("vcuser_benefit/count")
    public R vcuserBenefitCount(int benefit){
        return xunyeeService.vcuserBenefitCount(benefit);
    }


    @ApiOperation("签到榜")
    @PassToken
    @GetMapping("person_check_count/rank")
    public R<ResRank<ResPersonCheckCount>> personCheckCountRank(HttpServletRequest request, @Valid ReqPersonCheckCount req){
        Integer userId= UserUtil.getUserId(request);
        return xunyeeService.personCheckCount(userId,req);
    }

    @ApiOperation("签到之前先获调用这个验证一下")
    @GetMapping("vcuser_person_check/verify")
    public R vcuserPersonCheckVerify(HttpServletRequest request,@Valid ReqPersonId req){
        int userId= UserUtil.getUserId(request);
        return xunyeeService.vcuserPersonCheckVerify(userId,req.getPerson());
    }

    @ApiOperation("签到")
    @PostMapping("vcuser_person_check")
    public R vcuserPersonCheck(HttpServletRequest request,@Valid ReqPersonId req){
        int userId= UserUtil.getUserId(request);
        return xunyeeService.vcuserPersonCheck(userId,req.getPerson());
    }

    @ApiOperation("我的爱豆")
    @GetMapping("person_check_count/idol")
    public R<ResRank<ResPersonCheckCountIdol>> personCheckCountIdol(HttpServletRequest request,ReqMyPage myPage){
        int userId= UserUtil.getUserId(request);
        return xunyeeService.personCheckCountIdol(userId,myPage);
    }


    @ApiOperation("取消关注某个艺人")
    @PutMapping("vcuser_person")
    public R vcuserPerson(HttpServletRequest request, @Valid ReqUserPersonCheck req){
        int userId= UserUtil.getUserId(request);
        return xunyeeService.vcuserPerson(userId,req);
    }

    @ApiOperation("权益价格")
    @PassToken
    @GetMapping("benefit_price/current")
    public R<ResXunyeeBenefitPrice> benefitPrice(){

        return xunyeeService.benefitPrice();
    }

    @ApiOperation("签到日历")
    @GetMapping("vcuser_person_check/calendar")
    public R<ResUserPersonCheckCalendar> vcuserPersonCheckCalendar(HttpServletRequest request,@Valid ReqPersonCheckCalendar req){
        int userId= UserUtil.getUserId(request);
        return xunyeeService.vcuserPersonCheckCalendar(userId,req);
    }


    @ApiOperation("明星详情页")
    @PassToken
    @GetMapping("vcuser_person/person_info")
    public R<ResPersonInfo> vcuserPersonPersonInfo(HttpServletRequest request,@Valid ReqPersonId req){
        Integer userId= UserUtil.getUserId(request);
        return xunyeeService.vcuserPersonPersonInfo(userId,req.getPerson());
    }

    @ApiOperation("单个艺人品牌带货排行")
    @PassToken
    @GetMapping("vcuser_person/person_brand")
    public R<Map<String,Object>> vcuserPersonPersonBrand(@Valid ReqPersonId req){
        return xunyeeService.vcuserPersonPersonBrand(req.getPerson());
    }


    @ApiOperation("明星曲线")
    @PassToken
    @GetMapping("report_person/rpt_trend_all")
    public R<List<ResPersonCurve>> reportPersonRptTrendAll(@Valid ReqPersonQuxian req){
        return xunyeeService.reportPersonRptTrendAll(req.getPerson());
    }

    @ApiOperation("真爱排行")
    @PassToken
    @GetMapping("report_person/fans_rank")
    public R<List<ResPersonFansRank>> reportPersonRptFansRank(@Valid ReqPersonId req){
        return xunyeeService.reportPersonRptFansRank(req.getPerson());
    }


    @ApiOperation("明星相册")
    @PassToken
    @GetMapping("vcuser_person/person_album")
    public R reportPersonAlbum(ReqMyPage myPage,@Valid ReqPersonId req){
        return xunyeeService.reportPersonAlbum(myPage,req.getPerson());
    }

    @ApiOperation("兑换券")
    @PostMapping("vcuser_benefit/voucher")
    public R vcuserBenefitVoucher(HttpServletRequest request,@Valid ReqVoucher req){
        int userId= UserUtil.getUserId(request);
        return xunyeeService.vcuserBenefitVoucher(userId,req.getVoucher());
    }

    @ApiOperation("微信支付统一下单")
    @PostMapping("vcuser_benefit_payorder/submit")
    public R vcuserBenefitPayOrderSubmit(HttpServletRequest request,@Valid ReqBenefitPayOrder req){
        int userId= UserUtil.getUserId(request);
        return xunyeeService.vcuserBenefitPayOrderSubmit(request,userId,req);
    }


    @ApiOperation("(综合搜索)动态/艺人")
    @PassToken
    @GetMapping("global/search")
    public R<Map<String,Object>> globalSearch(HttpServletRequest request,ReqMyPage myPage,@Valid ReqGlobalSearch reqGlobalSearch){
        Integer userId= UserUtil.getUserId(request);
        return xunyeeService.globalSearch(userId,myPage,reqGlobalSearch);
    }

    @ApiOperation("搜索动态")
    @PassToken
    @GetMapping("blog/search")
    public R<IPage<ResBlogPage>> blogSearch(HttpServletRequest request,ReqMyPage myPage,@Valid ReqGlobalSearch reqGlobalSearch){
        Integer userId= UserUtil.getUserId(request);
        return xunyeeService.blogSearch(userId,myPage,reqGlobalSearch);
    }




}
