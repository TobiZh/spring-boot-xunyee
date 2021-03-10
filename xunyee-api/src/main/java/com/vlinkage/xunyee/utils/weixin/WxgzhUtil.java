package com.vlinkage.xunyee.utils.weixin;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WxgzhUtil {
    @Autowired
    private static final Logger log = LoggerFactory.getLogger(WxgzhUtil.class);
    private static Token token = null;
 
    /**
     * http请求方法
     * @param requestUrl
     * @param requestMethod
     * @param outputStr
     * @return
     */
    public static String httpsRequest(String requestUrl, String requestMethod, String outputStr) {
        try {
            URL url = new URL(requestUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod(requestMethod);
            conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            if (outputStr != null) {
                OutputStream outputStream = conn.getOutputStream();
 
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }
            InputStream inputStream = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            StringBuffer buffer = new StringBuffer();
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            conn.disconnect();
            return buffer.toString();
        } catch (ConnectException ce) {
            System.out.println("连接超时：{}");
        } catch (Exception e) {
            System.out.println("https请求异常：{}");
        }
        return null;
    }
 
    /**
     * 发送模板消息
     *
     * @param template
     * @return
     */
    public static boolean sendTemplateMsg(Template template) {

        RestTemplate restTemplate=new RestTemplate();

        Map<String,Object> tMap=new HashMap<>();
        tMap.put("grant_type","client_credential");
        tMap.put("appid","wx34c44fdb00777651");
        tMap.put("secret","5bde3751fd95668a029f2aaaa29ee769");

        Map<String,Object> tResult=restTemplate.getForObject("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wx34c44fdb00777651&secret=5bde3751fd95668a029f2aaaa29ee769",Map.class);
        String token=tResult.get("access_token").toString();
//
        log.debug("token:{}",token);


        boolean flag = false;
        String requestUrl = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=ACCESS_TOKEN";
        requestUrl = requestUrl.replace("ACCESS_TOKEN", token);
        String Result = httpsRequest(requestUrl, "POST", template.toJSON());
        System.out.println(Result);

        JSONObject jsonResult= JSONObject.parseObject(Result);

        if (jsonResult != null) {
            int errorCode = (int) jsonResult.get("errcode");
            String errorMessage = jsonResult.getString("errmsg");
            if (errorCode == 0) {
                flag = true;
            } else {
                System.out.println("模板消息发送失败:" + errorCode + "," + errorMessage);
            }
        }
        return flag;
    }

    // 创建订单成功
    public static void sendTemplateOrderCreate(String openid,String price,String orderNo,String time) {
        Template template=new Template();
        //这里填写模板ID
        template.setTemplate_id("HEKXmP0vdLvt773awobE7HYoi_RBphVGchv87XK9O8c");
        //这里填写用户的openid
        template.setTouser(openid);
        //这里填写点击订阅消息后跳转小程序的界面
        List<TemplateParam> paras=new ArrayList<>();
        paras.add(new TemplateParam("character_string6",orderNo));
        paras.add(new TemplateParam("thing2","甜录"));
        paras.add(new TemplateParam("date4",time));
        paras.add(new TemplateParam("amount3",price));
        template.setTemplateParamList(paras);
        sendTemplateMsg(template);
    }

    // 支付成功
    public static void sendTemplateOrderPayed(String openid,String price,String orderNo,String time) {
        Template template=new Template();
        //这里填写模板ID
        template.setTemplate_id("BgtKsIloa8jBEtoagRHvMDQrBMY2Oowlnz5WptVhgfw");
        //这里填写用户的openid
        template.setTouser(openid);
        //这里填写点击订阅消息后跳转小程序的界面
        List<TemplateParam> paras=new ArrayList<>();
        paras.add(new TemplateParam("thing5","甜录"));
        paras.add(new TemplateParam("amount1",price));
        paras.add(new TemplateParam("character_string3",orderNo));
        paras.add(new TemplateParam("date2",time));
        paras.add(new TemplateParam("thing8","等待明星接单"));
        template.setTemplateParamList(paras);
        sendTemplateMsg(template);
    }
}