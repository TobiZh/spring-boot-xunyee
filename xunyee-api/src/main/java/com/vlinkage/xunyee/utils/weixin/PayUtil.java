package com.vlinkage.xunyee.utils.weixin;

import com.github.binarywang.wxpay.bean.profitsharing.ProfitSharingReceiverRequest;
import com.github.binarywang.wxpay.bean.profitsharing.ProfitSharingReceiverResult;
import com.github.binarywang.wxpay.bean.profitsharing.ProfitSharingRequest;
import com.github.binarywang.wxpay.bean.profitsharing.ProfitSharingResult;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.ProfitSharingService;
import com.github.binarywang.wxpay.service.WxPayService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PayUtil {



    //https://api.mch.weixin.qq.com/pay/profitsharingaddreceiver

    @Autowired
    private WxPayService wxPayService;


    private String appAppId="wxf71bf12e04438f45";
    private String mpAppId="wx849245fa6b1e3cd3";
    private String mchId="1601992202";
    private String fmchId="1604364984";
    private String subMchId="1604833832";
    private String mchKey="Travels7998xuzuolin424345Travels";

    public WxPayConfig getConfig(String appId,String mchId,String subMchId,String mchKey){
        WxPayConfig config=new WxPayConfig();
        config.setAppId(appId);
        config.setMchId(mchId);
        if(StringUtils.isNotBlank(subMchId)){
            config.setSubMchId(subMchId);
        }
        config.setMchKey(mchKey);
        return config;
    }

    /**
     * 添加分账接收方
     * @param appid 微信分配的服务商appid
     * @param mchId 微信支付分配的服务商商户号
     * @param subMchId 微信支付分配的子商户号，即分账的出资商户号。
     * @param receiver
     *  <receiver>
     * {
     *        "type": "MERCHANT_ID",
     *        "account": "190001001",
     *        "name": "示例商户全称",
     * "relation_type": "STORE_OWNER"
     * }
     */
    public ProfitSharingReceiverResult profitsharingaddreceiver(String appid,String mchId,String subMchId,String receiver){

        ProfitSharingReceiverRequest request=new ProfitSharingReceiverRequest();
        request.setAppid(appid);
        request.setMchId(mchId);
        request.setSubMchId(subMchId);
        request.setNonceStr(WeiXinUtil.getRandomStringByLength(32));

        request.setReceiver(receiver);
        ProfitSharingService profitSharingService =wxPayService.getProfitSharingService();
        try {
            ProfitSharingReceiverResult result=profitSharingService.addReceiver(request);

            return result;
        } catch (WxPayException e) {
            e.printStackTrace();
        }
        return null;
    }


    //https://api.mch.weixin.qq.com/secapi/pay/profitsharing


    /**
     * 请求单次分账
     * @param appid 微信分配的服务商appid
     * @param mchId 微信支付分配的服务商商户号
     * @param subMchId 微信支付分配的子商户号，即分账的出资商户号。
     * @param transactionId 微信支付订单id
     * @param outOrderNo 商户分账单号
     * @param receivers 分账接收方列表
     *  <receivers>
     *   [
     *     {
     *          "type": "MERCHANT_ID",
     *          "account":"190001001",
     *          "amount":100,
     *          "description": "分到商户"
     * },
     *     {
     *          "type": "PERSONAL_OPENID",
     *          "account":"86693952",
     *          "amount":888,
     *          "description": "分到个人"
     * }
     * ]
     *   </receivers>
     *
     */
    public void profitsharing(String appid,String mchId,String subMchId,String transactionId,String outOrderNo,String receivers){
        ProfitSharingRequest request=new ProfitSharingRequest();
        request.setAppid(appid);
        request.setMchId(mchId);
        request.setSubMchId(subMchId);
        request.setNonceStr(WeiXinUtil.getRandomStringByLength(32));

        request.setReceivers(receivers);
        ProfitSharingService profitSharingService =wxPayService.getProfitSharingService();
        try {
            ProfitSharingResult result=profitSharingService.profitSharing(request);

        } catch (WxPayException e) {
            e.printStackTrace();
        }
    }

}
