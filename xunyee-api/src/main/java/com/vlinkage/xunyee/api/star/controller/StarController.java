package com.vlinkage.xunyee.api.star.controller;


import com.vlinkage.xunyee.api.star.service.StarService;
import com.vlinkage.xunyee.entity.response.ResSdbJdInfo;
import com.vlinkage.xunyee.entity.response.ResSdbJdPersonBrand;
import com.vlinkage.xunyee.entity.response.ResSdbJdSale;
import com.vlinkage.xunyee.entity.result.R;
import com.vlinkage.xunyee.jwt.PassToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "品牌星")
@RequestMapping("brand_star")
@RestController
public class StarController {

    @Autowired
    private StarService starService;

    @ApiOperation(value = "开始结束日期，前三艺人头像",notes = "返回的艺人信息已经经过排序")
    @PassToken
    @GetMapping("info")
    public R<ResSdbJdInfo> brandStarInfo(){

        return starService.brandStarInfo();
    }

    @ApiOperation(value = "带货排行榜柱状图",notes = "返回字段与1.0版本一致" )
    @PassToken
    @GetMapping("rate")
    public R<List<ResSdbJdSale>> brandStarRate(){

        return starService.brandStarRate();
    }

    @ApiOperation(value = "艺人品牌列表",notes = "与1.0有区别，将艺人代言的品牌list嵌套在艺人list" )
    @PassToken
    @GetMapping("rank")
    public R<List<ResSdbJdPersonBrand>> brandStarRank(String name){

        return starService.brandStarRank(name);
    }


}
