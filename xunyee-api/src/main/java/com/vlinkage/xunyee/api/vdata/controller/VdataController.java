package com.vlinkage.xunyee.api.vdata.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vlinkage.xunyee.entity.request.ReqPersonCheckCount;
import com.vlinkage.xunyee.entity.response.ResPersonCheckCount;
import com.vlinkage.xunyee.entity.result.R;
import com.vlinkage.xunyee.api.vdata.service.VdataService;
import com.vlinkage.xunyee.entity.request.ReqReportPersonRptTrend;
import com.vlinkage.xunyee.entity.request.ReqReportTeleplayRptTrend;
import com.vlinkage.xunyee.entity.request.ReqReportZyRptTrend;
import com.vlinkage.xunyee.entity.response.ResRank;
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


@Api(tags = "榜单指数")
@RequestMapping("vdata")
@RestController
public class VdataController {

    @Autowired
    private VdataService vdataService;

    @ApiOperation("签到榜")
    @PassToken
    @GetMapping("person_check_count/rank")
    public R<ResRank<ResPersonCheckCount>> personCheckCountRank(HttpServletRequest request, @Valid ReqPersonCheckCount req){
        Integer userId= UserUtil.getUserId(request);
        return vdataService.personCheckCount(userId,req);
    }

    @ApiOperation("电视剧艺人指数")
    @PassToken
    @GetMapping("report_person/rpt_trend")
    public R<IPage<ResRank>> reportPersonRptTrend(@Valid ReqReportPersonRptTrend req){

        return vdataService.reportPersonRptTrend(req);
    }

    @ApiOperation("综艺嘉宾指数")
    @PassToken
    @GetMapping("report_person/rpt_trend_zy")
    public R<IPage<ResRank>> reportPersonRptTrendZy(@Valid ReqReportPersonRptTrend req){

        return vdataService.reportPersonRptTrendZy(req);
    }

    @ApiOperation("电视剧指数")
    @PassToken
    @GetMapping("report_teleplay/rpt_trend")
    public R<IPage<ResRank>> reportTeleplayRptTrend(@Valid ReqReportTeleplayRptTrend req){

        return vdataService.reportTeleplayRptTrend(req);
    }

    @ApiOperation("网剧指数")
    @PassToken
    @GetMapping("report_teleplay/rpt_trend_net")
    public R<IPage<ResRank>> reportTeleplayRptTrendNet(@Valid ReqReportTeleplayRptTrend req){

        return vdataService.reportTeleplayRptTrendNet(req);
    }

    @ApiOperation("综艺指数")
    @PassToken
    @GetMapping("report_zy/rpt_trend")
    public R<IPage<ResRank>> reportZyRptTrend(@Valid ReqReportZyRptTrend req){

        return vdataService.reportZyRptTrend(req);
    }

    @ApiOperation("网络综艺指数")
    @PassToken
    @GetMapping("report_zy/rpt_trend_net")
    public R<IPage<ResRank>> reportZyNetRptTrend(@Valid ReqReportZyRptTrend req){

        return vdataService.reportZyNetRptTrend(req);
    }

}
