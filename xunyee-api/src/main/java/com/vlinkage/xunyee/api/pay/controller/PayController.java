package com.vlinkage.xunyee.api.pay.controller;


import com.github.binarywang.wxpay.exception.WxPayException;
import com.vlinkage.xunyee.api.pay.service.PayService;
import com.vlinkage.xunyee.jwt.PassToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RequestMapping("pay")
@RestController
public class PayController {

    @Autowired
    private PayService payService;

    @ApiIgnore
    @PassToken
    @PostMapping("/notify/benefit/weixin")
    public String notifyBenefitWeixin(@RequestBody String xmlData) throws WxPayException {
        return payService.notifyBenefitWeixin(xmlData);
    }
}
