package com.vlinkage.xunyee.api.login.service;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vlinkage.ant.xunyee.entity.XunyeeVcuser;
import com.vlinkage.ant.xunyee.entity.XunyeeVcuserOauth;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.common.entity.result.code.ResultCode;
import com.vlinkage.common.redis.RedisUtil;
import com.vlinkage.xunyee.config.weixin.WxMaConfiguration;
import com.vlinkage.xunyee.entity.response.ResLoginSuccessApp;
import com.vlinkage.xunyee.entity.response.ResLoginSuccessMini;
import com.vlinkage.xunyee.jwt.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.service.WxOAuth2Service;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class LoginService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private WxMpService wxMpService;

    @Transactional
    public R<ResLoginSuccessApp> wxOpenLogin(String appId,String code,int site) {
        if (!this.wxMpService.switchover(appId)) {
            throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的配置，请核实！", appId));
        }

        try {
            // ======================= 通过微信接口获取微信用户信息 ===================================
            WxOAuth2Service wxOAuth2Service=wxMpService.getOAuth2Service();
            WxOAuth2AccessToken oAuth2AccessToken=wxOAuth2Service.getAccessToken(code);
            WxOAuth2UserInfo userInfo=wxOAuth2Service.getUserInfo(oAuth2AccessToken,"zh_CN");
            String unionid= userInfo.getUnionId();
            String openid=userInfo.getOpenid();
            String nickname=userInfo.getNickname();
            String avatar=userInfo.getHeadImgUrl();
            String city=userInfo.getCity();
            String country=userInfo.getCountry();
            String province=userInfo.getProvince();
            Integer sex=userInfo.getSex();
            // ======================= 通过微信接口获取微信用户信息 ===================================

            // ======================= 自己系统的登录逻辑 ===================================
            QueryWrapper qw = new QueryWrapper();
            qw.eq("unionid", unionid);
            qw.eq("openid", openid);
            XunyeeVcuserOauth temp = new XunyeeVcuserOauth().selectOne(qw);
            if (temp == null) {
                // 新增账号
                XunyeeVcuser user = new XunyeeVcuser();
                user.setNickname(nickname);
                user.setAvatar(avatar);
                user.setWx_city(city);
                user.setWx_country(country);
                user.setWx_province(province);
                user.setSex(sex);
                if (!user.insert()) {
                    return R.ERROR();
                }
                // 新增第三方账号
                XunyeeVcuserOauth userThird = new XunyeeVcuserOauth();
                userThird.setSite(site);
                userThird.setOpenid(openid);
                if (StringUtils.isNotEmpty(unionid)) {
                    userThird.setUnionid(unionid);
                }
                userThird.setVcuser_id(user.getId());
                userThird.insert();
                // 生成token
                String token = JwtUtil.getAccessToken(String.valueOf(user.getId()));
                String refresh_token = JwtUtil.getRefreshToken(String.valueOf(user.getId()));
                redisUtil.set("user_token:"+user.getId()+":access_token",token,24*60*60);
                redisUtil.set("user_token:"+user.getId()+"refresh_token",refresh_token,15*24*60*60);

                ResLoginSuccessApp resLoginSuccess = new ResLoginSuccessApp();
                resLoginSuccess.setToken(token);
                return R.OK(resLoginSuccess);
            }
            // 生成token
            String token = JwtUtil.getAccessToken(String.valueOf(temp.getVcuser_id()));
            String refresh_token = JwtUtil.getRefreshToken(String.valueOf(temp.getVcuser_id()));
            redisUtil.set("user_token:"+temp.getVcuser_id()+":access_token",token,24*60*60);
            redisUtil.set("user_token:"+temp.getVcuser_id()+":refresh_token",refresh_token,15*24*60*60);

            ResLoginSuccessApp resLoginSuccess = new ResLoginSuccessApp();
            resLoginSuccess.setToken(token);
            return R.OK(resLoginSuccess);
            // ======================= 自己系统的登录逻辑 ===================================
        } catch (WxErrorException e) {
            e.printStackTrace();
            return R.ERROR(e.getMessage());
        }
    }

    public R<ResLoginSuccessMini> wxLoginMini(String appId, String code,int site) {
        final WxMaService wxService = WxMaConfiguration.getMaService(appId);
        // 用户信息校验
        try {
            WxMaJscode2SessionResult session = wxService.getUserService().getSessionInfo(code);
            String sessionKey=session.getSessionKey();
            String unionid= session.getUnionid();
            String openid=session.getOpenid();

            // 每个客户端的 openid 都是唯一的
            QueryWrapper qw = new QueryWrapper();
            qw.eq("unionid", unionid);
            qw.eq("openid", openid);
            XunyeeVcuserOauth temp = new XunyeeVcuserOauth().selectOne(qw);
            if (temp == null) {
                // 新增账号
                XunyeeVcuser user = new XunyeeVcuser();
                user.setSex(1);//默认给1
                if (!user.insert()) {
                    return R.ERROR();
                }
                // 新增第三方账号
                XunyeeVcuserOauth userThird = new XunyeeVcuserOauth();
                userThird.setSite(site);
                userThird.setOpenid(openid);
                if (StringUtils.isNotEmpty(unionid)) {
                    userThird.setUnionid(unionid);
                }
                userThird.setVcuser_id(user.getId());
                userThird.insert();
                // 登录成功 生成token
                String token = JwtUtil.getAccessToken(String.valueOf(user.getId()));
                String refresh_token = JwtUtil.getRefreshToken(String.valueOf(user.getId()));
                redisUtil.set("user_token:"+user.getId()+":access_token",token,24*60*60);
                redisUtil.set("user_token:"+user.getId()+"refresh_token",refresh_token,15*24*60*60);

                ResLoginSuccessMini resLoginSuccess = new ResLoginSuccessMini();
                resLoginSuccess.setSession_key(sessionKey);
                resLoginSuccess.setToken(token);
                return R.OK(resLoginSuccess);
            }
            // 登录成功 生成token
            XunyeeVcuser vcuser=new XunyeeVcuser().selectById(temp.getVcuser_id());
            String token = JwtUtil.getAccessToken(String.valueOf(temp.getVcuser_id()));
            String refresh_token = JwtUtil.getRefreshToken(String.valueOf(temp.getVcuser_id()));
            redisUtil.set("user_token:"+temp.getVcuser_id()+":access_token",token,24*60*60);
            redisUtil.set("user_token:"+temp.getVcuser_id()+":refresh_token",refresh_token,15*24*60*60);


            ResLoginSuccessMini resLoginSuccess = new ResLoginSuccessMini();
            resLoginSuccess.setSession_key(sessionKey);
            resLoginSuccess.setToken(token);
            if (StringUtils.isNotEmpty(vcuser.getNickname())){
                resLoginSuccess.setNickname(vcuser.getNickname());
            }

            if (StringUtils.isNotEmpty(vcuser.getAvatar())){
                resLoginSuccess.setAvatar(vcuser.getAvatar());
            }

            return R.OK(resLoginSuccess);
        } catch (WxErrorException e) {
            log.error(e.getMessage(), e);
            return R.ERROR(e.getMessage());
        }
    }

    public R<ResLoginSuccessApp> refreshToken(String refreshToken) {
        if (!JwtUtil.verify(refreshToken)){
            return R.ERROR(ResultCode.NO_TOKEN_TO_LOGIN);
        }
        String userId = JwtUtil.getUserId(refreshToken);

        String token=JwtUtil.getAccessToken(userId);
        String refresh_token=JwtUtil.getRefreshToken(userId);
        redisUtil.set("user_token:"+userId+":access_token",token,24*60*60);
        redisUtil.set("user_token:"+userId+":refresh_token",refresh_token,15*24*60*60);


        ResLoginSuccessApp app=new ResLoginSuccessApp();
        app.setToken(token);
        app.setRefresh_token(refresh_token);
        return R.OK(app);

    }
}
