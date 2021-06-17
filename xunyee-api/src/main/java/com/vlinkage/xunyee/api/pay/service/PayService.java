package com.vlinkage.xunyee.api.pay.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.binarywang.wxpay.bean.WxPayApiData;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.order.WxPayAppOrderResult;
import com.github.binarywang.wxpay.bean.request.BaseWxPayRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderResult;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.vlinkage.ant.xunyee.entity.XunyeeBenefitPrice;
import com.vlinkage.ant.xunyee.entity.XunyeeVcuserBenefit;
import com.vlinkage.ant.xunyee.entity.XunyeeVcuserBenefitPayorder;
import com.vlinkage.ant.xunyee.mapper.XunyeeVcuserBenefitMapper;
import com.vlinkage.xunyee.entity.request.ReqBenefitPayOrder;
import com.vlinkage.xunyee.entity.result.R;
import com.vlinkage.xunyee.config.weixin.WxMpProperties;
import com.vlinkage.xunyee.config.weixin.WxPayProperties;
import com.vlinkage.xunyee.utils.OrderCodeFactory;
import com.vlinkage.xunyee.utils.weixin.WeiXinUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
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


    public R<WxPayAppOrderResult> vcuserBenefitPayOrderSubmit(HttpServletRequest request, int userId, ReqBenefitPayOrder req) {
        int site = req.getSite();

        XunyeeBenefitPrice price = new XunyeeBenefitPrice().selectById(req.getBenefit_price());
        if (price == null) {
            return R.ERROR("您购买的会员服务不存在");
        }
        // 生成一条付款记录 状态是未支付
        XunyeeVcuserBenefitPayorder payorder = new XunyeeVcuserBenefitPayorder();
        payorder.setVcuser_id(userId);
        payorder.setBenefit_price_id(price.getId());
        payorder.setIs_paid(false);
        payorder.setQuantity(price.getQuantity());
        payorder.setSite(site);
        payorder.setPrice(price.getPrice());
        String orderNo = OrderCodeFactory.getOrderCode((long) userId);
        payorder.setSite_transaction_id(orderNo);
        Date nowDate = new Date();
        payorder.setUpdated(nowDate);
        payorder.setCreated(nowDate);
        if (payorder.insert()) {
            return payBenefit(request, payorder);
        }
        return R.ERROR("下单失败");
    }


    // ===========================  商品支付 app  ===================================
    public R<WxPayAppOrderResult> payBenefit(HttpServletRequest request, XunyeeVcuserBenefitPayorder order) {

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

            orderRequest.setOutTradeNo(order.getSite_transaction_id());//商户订单号
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
            // 创建一个app支付
            WxPayAppOrderResult result = wxPayService.createOrder(orderRequest);
            return R.OK(result);
        } catch (Exception e) {
            log.error("微信预支付支付失败！订单号：{},原因:{}", order.getSite_transaction_id(), e.getMessage());
            e.printStackTrace();
            return R.ERROR(e.getMessage());
        }
    }

    public String notifyBenefitWeixin(String xmlData) throws WxPayException {
        log.error("微信支付回调");
        final WxPayOrderNotifyResult notifyResult = wxService.parseOrderNotifyResult(xmlData);
        String transactionId = notifyResult.getTransactionId();   // 微信交易号;
        String outTradeNo = notifyResult.getOutTradeNo();  // 用户订单号;
        BigDecimal totalFee = BigDecimal.valueOf(notifyResult.getTotalFee()).divide(new BigDecimal(100));

        LambdaQueryWrapper<XunyeeVcuserBenefitPayorder> qw = new LambdaQueryWrapper<>();
        qw.eq(XunyeeVcuserBenefitPayorder::getSite_transaction_id, outTradeNo);
        XunyeeVcuserBenefitPayorder order = new XunyeeVcuserBenefitPayorder().selectOne(qw);
        if (order != null) {
            if (order.getIs_paid()) {
                return WxPayNotifyResponse.success("此订单已经支付过了");
            }

            Date date = new Date();
            //order.setSite_transaction_id(transactionId);
            order.setUpdated(date);
            order.setIs_paid(true);
            if (order.updateById()) {
                int userId = order.getVcuser_id();
                int plusDays = order.getQuantity();

                XunyeeBenefitPrice benefitPrice = new XunyeeBenefitPrice().selectById(order.getBenefit_price_id());
                int benefitId = benefitPrice.getBenefit_id();
                LocalDate nowDate = LocalDate.now();


                LambdaQueryWrapper<XunyeeVcuserBenefit> bqw = new LambdaQueryWrapper();
                bqw.eq(XunyeeVcuserBenefit::getVcuser_id, userId)
                        .ge(XunyeeVcuserBenefit::getFinish_time, nowDate)
                        .orderByDesc(XunyeeVcuserBenefit::getFinish_time)
                        .last("limit 1");
                XunyeeVcuserBenefit temp = new XunyeeVcuserBenefit().selectOne(bqw);
                if (temp == null) {
                    XunyeeVcuserBenefit vcuserBenefit = new XunyeeVcuserBenefit();
                    vcuserBenefit.setId(UUID.randomUUID().toString());
                    vcuserBenefit.setBenefit_id(benefitId);
                    vcuserBenefit.setVcuser_id(userId);
                    vcuserBenefit.setUpdated(date);
                    vcuserBenefit.setCreated(date);
                    vcuserBenefit.setStart_time(nowDate);
                    vcuserBenefit.setFinish_time(nowDate.plusDays(plusDays));
                    if (!vcuserBenefit.insert()) {
                        return WxPayNotifyResponse.success("开通会员成功");
                    }
                } else {
                    // 这里使用xunyeeVcuserBenefitMapper 是因为 pgsql的主键使用的是uuid类型，不能updateById;
                    temp.setBenefit_id(benefitId);
                    temp.setUpdated(date);
                    // 结束时间延长
                    temp.setFinish_time(temp.getFinish_time().plusDays(plusDays));
                    LambdaUpdateWrapper<XunyeeVcuserBenefit> updateQw = new LambdaUpdateWrapper<>();
                    updateQw.eq(XunyeeVcuserBenefit::getId, UUID.fromString(temp.getId()));
                    int updatedCount=xunyeeVcuserBenefitMapper.update(temp,updateQw);
                    if (updatedCount>0) {
                        return WxPayNotifyResponse.success("开通会员成功");
                    }
                }
            }
            log.error("order.updateById:{}", JSONObject.toJSONString(order));
        }
        log.error("查不到订单号{}", JSONObject.toJSONString(outTradeNo));
        return WxPayNotifyResponse.fail("查不到订单号");
    }
    // ===========================  商品支付  ===================================
}
