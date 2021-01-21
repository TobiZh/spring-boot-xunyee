package com.vlinkage.xunyee.api.xunyee.controller;

import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.xunyee.service.XunyeeService;
import com.vlinkage.xunyee.entity.request.ReqPic;
import com.vlinkage.xunyee.entity.response.ResPic;
import com.vlinkage.xunyee.jwt.PassToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Api("寻艺接口")
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
}
