package com.vlinkage.xunyee.api.pay.controller;


import com.github.binarywang.wxpay.bean.order.WxPayAppOrderResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.vlinkage.xunyee.api.pay.service.PayService;
import com.vlinkage.xunyee.entity.request.ReqBenefitPayOrder;
import com.vlinkage.xunyee.entity.result.R;
import com.vlinkage.xunyee.jwt.PassToken;
import com.vlinkage.xunyee.utils.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Api(tags = "支付")
@RequestMapping("pay")
@RestController
public class PayController {

    @Autowired
    private PayService payService;

    @ApiOperation("微信支付统一下单")
    @PostMapping("vcuser_benefit_payorder/submit")
    public R<WxPayAppOrderResult> vcuserBenefitPayOrderSubmit(HttpServletRequest request, @Valid ReqBenefitPayOrder req){
        int userId= UserUtil.getUserId(request);
        return payService.vcuserBenefitPayOrderSubmit(request,userId,req);
    }

    @ApiIgnore
    @PassToken
    @PostMapping("/notify/benefit/weixin")
    public String notifyBenefitWeixin(@RequestBody String xmlData) throws WxPayException {
        return payService.notifyBenefitWeixin(xmlData);
    }
}
