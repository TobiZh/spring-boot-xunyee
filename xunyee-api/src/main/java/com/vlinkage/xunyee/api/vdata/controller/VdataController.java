package com.vlinkage.xunyee.api.vdata.controller;

import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.vdata.service.VdataService;
import com.vlinkage.xunyee.entity.request.ReqReportPersonRptTrend;
import com.vlinkage.xunyee.entity.request.ReqReportTeleplayRptTrend;
import com.vlinkage.xunyee.entity.request.ReqReportZyRptTrend;
import com.vlinkage.xunyee.jwt.PassToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Api(tags = "指数")
@RequestMapping("vdata")
@RestController
public class VdataController {

    @Autowired
    private VdataService vdataService;


    @ApiOperation("电视剧艺人")
    @PassToken
    @GetMapping("report_person/rpt_trend")
    public R reportPersonRptTrend(ReqReportPersonRptTrend req){

        return vdataService.reportPersonRptTrend(req);
    }

    @ApiOperation("综艺嘉宾指数")
    @PassToken
    @GetMapping("report_person/rpt_trend_zy")
    public R reportPersonRptTrendZy(ReqReportPersonRptTrend req){

        return vdataService.reportPersonRptTrendZy(req);
    }

    @ApiOperation("电视剧")
    @PassToken
    @GetMapping("report_teleplay/rpt_trend")
    public R reportTeleplayRptTrend(ReqReportTeleplayRptTrend req){

        return vdataService.reportTeleplayRptTrend(req);
    }

    @ApiOperation("网剧")
    @PassToken
    @GetMapping("report_teleplay/rpt_trend_net")
    public R reportTeleplayRptTrendNet(ReqReportTeleplayRptTrend req){

        return vdataService.reportTeleplayRptTrendNet(req);
    }

    @ApiOperation("综艺")
    @PassToken
    @GetMapping("report_zy/rpt_trend")
    public R reportZyRptTrend(ReqReportZyRptTrend req){

        return vdataService.reportZyRptTrend(req);
    }

    @ApiOperation("网络综艺")
    @PassToken
    @GetMapping("report_zy/rpt_trend_net")
    public R reportZyNetRptTrend(ReqReportZyRptTrend req){

        return vdataService.reportZyNetRptTrend(req);
    }

}
