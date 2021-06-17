package com.vlinkage.xunyee.api.xunyee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vlinkage.xunyee.entity.result.R;
import com.vlinkage.xunyee.api.provide.MetaService;
import com.vlinkage.xunyee.api.xunyee.service.XunyeeService;
import com.vlinkage.xunyee.entity.ReqMyPage;
import com.vlinkage.xunyee.entity.request.*;
import com.vlinkage.xunyee.entity.response.*;
import com.vlinkage.xunyee.jwt.PassToken;
import com.vlinkage.xunyee.utils.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
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
    @ApiImplicitParam(name = "source",value = "android,ios,mini")
    @PassToken
    @GetMapping("navigation/{source}")
    public R<List<ResNavigation>> getNavigation(@PathVariable("source") String source){

        return xunyeeService.getNavigation(source);
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

    @ApiOperation("艺人相关的品牌(动态类型-品牌代言)")
    @PassToken
    @GetMapping("brand/person")
    public R<List<ResBrandPerson>> brandPerson(@Valid ReqPersonId req){
        List<ResBrandPersonList> brands=metaService.getPersonBrandList(req.getPerson());

        return R.OK(brands);
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


    @ApiOperation(value = "签到之前先获调用这个验证一下",notes = "通过 code 来控制弹窗，" +
            "code = 0 弹 去签到，data返回一个对象；前端通过判断 data是否为空来控制显示隐藏，" +
            "data为空的时候隐藏广告链接；data不为空的时候显示广告链接，" +
            "code = -1 弹 toast" +
            "code = 30001 弹 登录，" +
            "code = 20004 弹 开通会员...")
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

    @ApiOperation(value = "权益价格",notes = "benefit 可以不传 ，不传默认为1，以三合一签到即benefit=1为例，在返回结果中，id是该权益价格的主键，" +
            "也是后文提交订单时参数benefit_price的值。price是该权益价格的实际价格，tag_price是该权益价格的划线价格。quantity是该权益价格的天数。" +
            "比如月卡的quantity为30，季卡的的quantity为90。" +
            "返回结果的顺序是按quantity升序排列。")
    @PassToken
    @GetMapping("benefit_price/current")
    public R<ResXunyeeBenefitPrice> benefitPrice(Integer benefit){

        return xunyeeService.benefitPrice(benefit);
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
    public R<ResPersonBrandInfo> vcuserPersonPersonBrand(@Valid ReqPersonId req){
        return xunyeeService.vcuserPersonPersonBrand(req.getPerson());
    }


    @ApiOperation("明星曲线")
    @PassToken
    @GetMapping("report_person/rpt_trend_all")
    public R<List<ResPersonCurve>> reportPersonRptTrendAll(@Valid ReqPersonQuxian req){
        return xunyeeService.reportPersonRptTrendAll(req);
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

    @ApiOperation("用户隐私政策和协议")
    @PassToken
    @GetMapping("ver/agreement")
    public R agreement(int t) throws IOException {

        return xunyeeService.agreement(t);
    }

    @ApiOperation("寻艺app检查更新")
    @PassToken
    @GetMapping("version/check")
    public R<ResAppVersion> appVersionCheck(Integer version_code) {

        return xunyeeService.appVersionCheck(version_code);
    }

    @ApiOperation("添加一条品牌浏览记录")
    @PostMapping("brand/brow")
    public R brandBrow(HttpServletRequest request,int brand_id){
        int userId= UserUtil.getUserId(request);
        return xunyeeService.brandBrow(userId,brand_id);
    }

    @ApiOperation("获取品牌浏览记录")
    @GetMapping("brand/brow/history")
    public R<List<ResBrandPersonList>> brandBrowHistory(HttpServletRequest request){
        int userId= UserUtil.getUserId(request);
        return xunyeeService.brandBrowHistory(userId);
    }



}
