package com.vlinkage.xunyee.api.pay.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.profitsharing.ProfitSharingReceiverRequest;
import com.github.binarywang.wxpay.bean.profitsharing.ProfitSharingReceiverResult;
import com.github.binarywang.wxpay.bean.request.BaseWxPayRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderResult;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.ProfitSharingService;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.google.gson.Gson;
import com.vlinkage.ant.xunyee.entity.XunyeeBenefitPrice;
import com.vlinkage.ant.xunyee.entity.XunyeeVcuserBenefit;
import com.vlinkage.ant.xunyee.entity.XunyeeVcuserBenefitPayorder;
import com.vlinkage.ant.xunyee.mapper.XunyeeVcuserBenefitMapper;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.config.weixin.WxMpProperties;
import com.vlinkage.xunyee.config.weixin.WxPayProperties;
import com.vlinkage.xunyee.utils.weixin.WeiXinUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class PayService {

    @Value("${xunyee.config.host}")
    private String host;

    @Autowired
    private WxPayProperties wxPayProperties;
    @Autowired
    private WxMpProperties wxMpProperties;

    @Autowired
    private WxPayService wxService;

    @Resource
    private XunyeeVcuserBenefitMapper xunyeeVcuserBenefitMapper;

    // ===========================  商品支付  ===================================
    public R payBenefit(HttpServletRequest request, XunyeeVcuserBenefitPayorder order) {

        if (order.getIs_paid()) {
            return R.ERROR("该订单无需支付");
        }
        String tradeType = WxPayConstants.TradeType.APP;
        String appId = wxMpProperties.getConfigs().get(0).getAppid();//app的appid
        String mchId = wxPayProperties.getMchId();
        String mchKey = wxPayProperties.getMchKey();
        try {
            WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
            orderRequest.setBody("寻艺会员");

            orderRequest.setOutTradeNo(String.valueOf(order.getRel_order_id()));//商户订单号
            orderRequest.setTotalFee(BaseWxPayRequest.yuanToFen(String.valueOf(order.getPrice())));//元转成分
            orderRequest.setNonceStr(WeiXinUtil.getRandomStringByLength(32));//随机字符串
            orderRequest.setSpbillCreateIp(WeiXinUtil.getIpAddr(request));//ip地址

            // ------------------- 微信支付的参数配置 -------------------
            WxPayConfig payConfig = new WxPayConfig();
            payConfig.setNotifyUrl(host + "/pay/notify/benefit/weixin");
            payConfig.setTradeType(tradeType);
            payConfig.setAppId(appId);
            payConfig.setMchId(mchId);
            payConfig.setMchKey(mchKey);
            // ------------------- 微信支付的参数配置 -------------------
            WxPayService wxPayService = new WxPayServiceImpl();
            wxPayService.setConfig(payConfig);
            WxPayUnifiedOrderResult result = wxPayService.createOrder(orderRequest);
            return R.OK(result);
        } catch (Exception e) {
            log.error("微信支付失败！订单号：{},原因:{}", order.getRel_order_id(), e.getMessage());
            e.printStackTrace();
            return R.ERROR(e.getMessage());
        }
    }

    public String notifyBenefitWeixin(String xmlData) throws WxPayException {
        final WxPayOrderNotifyResult notifyResult = wxService.parseOrderNotifyResult(xmlData);
        String transactionId = notifyResult.getTransactionId();   // 微信交易号;
        String outTradeNo = notifyResult.getOutTradeNo();  // 用户订单号;

        BigDecimal totalFee = BigDecimal.valueOf(notifyResult.getTotalFee()).divide(new BigDecimal(100));


        QueryWrapper qw = new QueryWrapper();
        qw.eq("rel_order_id", outTradeNo);
        XunyeeVcuserBenefitPayorder order = new XunyeeVcuserBenefitPayorder().selectOne(qw);
        if (order != null) {
            Date date = new Date();
            order.setSite_transaction_id(transactionId);
            order.setUpdated(date);
            order.setIs_paid(true);
            if (order.updateById()) {
                int userId=order.getVcuser_id();
                int plusDays = order.getQuantity();


                XunyeeBenefitPrice benefitPrice=new XunyeeBenefitPrice().selectById(order.getBenefit_price_id());
                int benefitId=benefitPrice.getBenefit_id();


                LocalDate nowDate = LocalDate.now();
                QueryWrapper bqw = new QueryWrapper();
                bqw.eq("vcuser_id", userId);
                bqw.ge("finish_time", nowDate);// <=
                XunyeeVcuserBenefit temp = new XunyeeVcuserBenefit().selectOne(bqw);

                if (temp == null) {
                    XunyeeVcuserBenefit vcuserBenefit = new XunyeeVcuserBenefit();
                    vcuserBenefit.setId(UUID.randomUUID().toString());
                    vcuserBenefit.setBenefit_id(benefitId);
                    vcuserBenefit.setVcuser_id(userId);
                    vcuserBenefit.setUpdated(date);
                    vcuserBenefit.setCreated(date);
                    vcuserBenefit.setFinish_time(nowDate);
                    vcuserBenefit.setFinish_time(nowDate.plusDays(plusDays));
                    if (!vcuserBenefit.insert()) {
                        return WxPayNotifyResponse.fail("支付失败");
                    }
                } else {

                    // 这里使用xunyeeVcuserBenefitMapper 是因为 pgsql的主键使用的是uuid类型，不能updateById;
                    QueryWrapper updateQw = new QueryWrapper();
                    updateQw.eq("id", UUID.fromString(temp.getId()));
                    temp.setBenefit_id(benefitId);
                    temp.setUpdated(date);
                    temp.setFinish_time(nowDate.plusDays(plusDays));
                    if (xunyeeVcuserBenefitMapper.update(temp, updateQw) <= 0) {
                        return WxPayNotifyResponse.fail("支付失败");
                    }
                }


                return WxPayNotifyResponse.success("成功");
            }
        }
        log.info("查不到订单号{}", JSONObject.toJSONString(outTradeNo));
        return WxPayNotifyResponse.fail("支付失败");
    }
    // ===========================  商品支付  ===================================
}
