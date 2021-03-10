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
import com.vlinkage.ant.xunyee.entity.XunyeeVcuserBenefitPayorder;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.utils.weixin.WeiXinUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PayService {

    @Value("${temple.config.host}")
    private String host;

    // ===========================  商品支付  ===================================
    public R payBenefit(HttpServletRequest request, XunyeeVcuserBenefitPayorder order) {

        if (order.getIs_paid()) {
            return R.ERROR("该订单无需支付");
        }
        String tradeType = WxPayConstants.TradeType.APP;
        String appId = APP_APP_ID;
        try {
            WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
            orderRequest.setBody("寻艺会员");

            orderRequest.setOutTradeNo(String.valueOf(order.getRel_order_id()));//商户订单号
            orderRequest.setTotalFee(BaseWxPayRequest.yuanToFen(String.valueOf(order.getPrice())));//元转成分
            orderRequest.setNonceStr(WeiXinUtil.getRandomStringByLength(32));//随机字符串
            orderRequest.setSpbillCreateIp(WeiXinUtil.getIpAddr(request));//ip地址

            // ------------------- 微信支付的参数配置 -------------------
            WxPayConfig payConfig = new WxPayConfig();
            payConfig.setNotifyUrl(host + "/pay/notify/goods/weixin");
            payConfig.setTradeType(tradeType);
            payConfig.setAppId(appId);
            payConfig.setMchId(mchId);
            payConfig.setMchKey(mchKey);
            // ------------------- 微信支付的参数配置 -------------------



            WxPayService wxPayService = new WxPayServiceImpl();
            wxPayService.setConfig(payConfig);
            WxPayUnifiedOrderResult result = wxPayService.unifiedOrder(orderRequest);
            MchPayApp mchPayApp = PayUtil.generateMchAppData(result.getPrepayId(), appId, mchId, mchKey);
            return R.OK(mchPayApp);
        } catch (Exception e) {
            log.error("微信支付失败！订单号：{},原因:{}", order.getOrderNo(), e.getMessage());
            e.printStackTrace();
            return R.ERROR(e.getMessage());
        }
    }

    public String notifyGoodsWeixin(String xmlData) throws WxPayException {
        final WxPayOrderNotifyResult notifyResult = wxService.parseOrderNotifyResult(xmlData);
        String mchId = notifyResult.getMchId();
        String subMchId = notifyResult.getSubMchId();
        String appid = notifyResult.getAppid();
        String transactionId = notifyResult.getTransactionId();   // 微信交易号;
        String outTradeNo = notifyResult.getOutTradeNo();  // 用户订单号;

        BigDecimal totalFee = BigDecimal.valueOf(notifyResult.getTotalFee()).divide(new BigDecimal(100));

        QueryWrapper qw = new QueryWrapper();
        qw.eq("order_no", outTradeNo);
        TmpOrder order = new TmpOrder().selectOne(qw);
        if (order != null) {
            order.setTransactionId(transactionId);
            order.setPaymentTime(LocalDateTime.now());
            order.setPayPrice(totalFee);
            order.setStatus(OrderEnum.ORDER_USER_PAID.getCode());
            if (order.updateById()) {
                // 更新商品销量
                TmpGoodsSub goodsSub = new TmpGoodsSub().selectById(order.getGoodsSubId());
                goodsSub.setSold(goodsSub.getSold() + order.getNumber());
                goodsSub.updateById();
                // 更新商品销量
                TmpGoods goods = new TmpGoods().selectById(order.getGoodsId());
                goods.setSold(goods.getSold() + order.getNumber());
                goods.updateById();

                // 记录一条分账记录
                if (order.getTempleId() > 0) {
                    int payType = order.getPayType();
                    TmpData tmpData = new TmpData().selectById(order.getTempleId());
                    if ((payType == 1 && tmpData.getIsOpenShareApp() == 1) || (payType == 3 && tmpData.getIsOpenShareJsapi() == 1)) {
                        BigDecimal amount = totalFee.multiply(tmpData.getShareScale()).setScale(2,BigDecimal.ROUND_HALF_UP);
                        if (amount.compareTo(BigDecimal.valueOf(0.01)) == 1) { //最低分账1分钱

                            ProfitSharingReceiverRequest request = new ProfitSharingReceiverRequest();
                            request.setAppid(appid);
                            request.setMchId(mchId);
                            request.setSubMchId(subMchId);
                            request.setNonceStr(WeiXinUtil.getRandomStringByLength(32));
                            //============== Receiver ====================
                            WxPayReceiver payReceiver = new WxPayReceiver();
                            payReceiver.setType("MERCHANT_ID");
                            payReceiver.setAccount(MCH_ID);
                            payReceiver.setName("在东方文化产业发展（云南）有限公司");
                            payReceiver.setRelation_type("SERVICE_PROVIDER");
                            request.setReceiver(new Gson().toJson(payReceiver));
                            //============== Receiver ====================

                            //============== Sign ====================
                            Map<String, String> wx_map = new HashMap<>();
                            wx_map.put("mch_id", mchId);
                            wx_map.put("sub_mch_id", subMchId);
                            wx_map.put("appid", appid);
                            wx_map.put("nonce_str", request.getNonceStr());
                            wx_map.put("receiver", request.getReceiver());
                            wx_map.put("sign_type", "HMAC-SHA256");
                            String sign = SignatureUtil.generateSign(wx_map, MCH_KEY);
                            //============== Sign ====================

                            request.setSignType("HMAC-SHA256");
                            request.setSign(sign);

                            WxPayService wxPayService = new WxPayServiceImpl();

                            WxPayConfig config = new WxPayConfig();
                            config.setMchKey(MCH_KEY);
                            config.setAppId(appid);
                            config.setMchId(mchId);
                            config.setSubMchId(subMchId);
                            config.setKeyPath("classpath:/static/apiclient_cert.p12");
                            wxPayService.setConfig(config);
                            ProfitSharingService profitSharingService = wxPayService.getProfitSharingService();
                            try {
                                ProfitSharingReceiverResult result = profitSharingService.addReceiver(request);
                                if (result.getResultCode().equals("SUCCESS")) {
                                    //添加一条分账记录
                                    ProfitSharingHistory sharingHistory = new ProfitSharingHistory();
                                    sharingHistory.setMchId(result.getMchId());
                                    sharingHistory.setSubMchId(result.getSubMchId());
                                    sharingHistory.setAmount(amount);
                                    sharingHistory.setTransactionId(transactionId);
                                    sharingHistory.insert();
                                }

                            } catch (WxPayException e) {
                                e.printStackTrace();
                            }
                        }
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
