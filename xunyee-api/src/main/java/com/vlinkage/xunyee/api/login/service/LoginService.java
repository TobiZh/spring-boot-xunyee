package com.vlinkage.xunyee.api.login.service;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.vlinkage.ant.xunyee.entity.XunyeeVcuser;
import com.vlinkage.ant.xunyee.entity.XunyeeVcuserOauth;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.common.redis.RedisUtil;
import com.vlinkage.xunyee.config.weixin.WxMaConfiguration;
import com.vlinkage.xunyee.entity.response.ResLoginSuccess;
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

import javax.annotation.Resource;

@Slf4j
@Service
public class LoginService {

    @Resource
    private RedisUtil redisUtil;

    @Autowired
    private WxMpService wxMpService;

    @Transactional
    public R wxOpenLogin(String appId,String code) {
        if (!this.wxMpService.switchover(appId)) {
            throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的配置，请核实！", appId));
        }

        try {
            WxOAuth2Service wxOAuth2Service=wxMpService.getOAuth2Service();
            WxOAuth2AccessToken oAuth2AccessToken=wxOAuth2Service.getAccessToken(code);
            WxOAuth2UserInfo userInfo=wxOAuth2Service.getUserInfo(oAuth2AccessToken,"zh_CN");
            String unionid= userInfo.getUnionId();
            String openid=userInfo.getOpenid();
            return doLoginMethod(unionid,openid,
                    userInfo.getNickname(),
                    userInfo.getHeadImgUrl(),
                    userInfo.getCity(),
                    userInfo.getCountry(),
                    userInfo.getProvince(),
                    userInfo.getSex());

        } catch (WxErrorException e) {
            e.printStackTrace();
            return R.ERROR(e.getMessage());
        }
    }

    public R wxOpenLoginMini(String appId, String code,String signature, String rawData, String encryptedData, String iv) {
        final WxMaService wxService = WxMaConfiguration.getMaService(appId);
        // 用户信息校验
        try {
            WxMaJscode2SessionResult session = wxService.getUserService().getSessionInfo(code);
            String sessionKey=session.getSessionKey();
            if (!wxService.getUserService().checkUserInfo(sessionKey, rawData, signature)) {
                return R.ERROR("user check failed");
            }
            // 解密用户信息
            WxMaUserInfo userInfo = wxService.getUserService().getUserInfo(sessionKey, encryptedData, iv);
            // 每个客户端的 openid 都是唯一的
            String unionid= userInfo.getUnionId();
            String openid=userInfo.getOpenId();

            return doLoginMethod(unionid,openid,
                    userInfo.getNickName(),
                    userInfo.getAvatarUrl(),
                    userInfo.getCity(),
                    userInfo.getCountry(),
                    userInfo.getProvince(),
                    Integer.valueOf(userInfo.getGender()));

        } catch (WxErrorException e) {
            log.error(e.getMessage(), e);
            return R.ERROR(e.getMessage());
        }
    }

    private R doLoginMethod(String unionid,String openid,String nickname,String avatar,String city,String country,String province,Integer sex){
        // 每个客户端的 openid 都是唯一的
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
            if (user.insert()) {
                // 新增第三方账号
                XunyeeVcuserOauth userThird = new XunyeeVcuserOauth();
                userThird.setSite(5);
                userThird.setOpenid(openid);
                if (StringUtils.isNotEmpty(unionid)) {
                    userThird.setUnionid(unionid);
                }
                userThird.setVcuser_id(user.getId());
                userThird.insert();

                // 登录成功 生成token
                String token = JwtUtil.getToken(String.valueOf(user.getId()));
                redisUtil.set("user_toke:"+temp.getVcuser_id(),token);
                ResLoginSuccess resLoginSuccess = new ResLoginSuccess();
                BeanUtil.copyProperties(user, resLoginSuccess);
                resLoginSuccess.setToken(token);
                return R.OK(resLoginSuccess);
            }

            return R.ERROR();

        } else {

            // 查询当前用户信息
            XunyeeVcuser user = new XunyeeVcuser().selectById(temp.getVcuser_id());
            // 1.先判断token是否过期
            String token = "";
            if (!redisUtil.hasKey("user_toke:"+temp.getVcuser_id())) {
                token = JwtUtil.getToken(String.valueOf(user.getId()));
                redisUtil.set("user_toke:"+temp.getVcuser_id(),token);
            }else{
                token=redisUtil.get("user_toke:"+temp.getVcuser_id()).toString();
            }
            // 登录成功 生成token


            ResLoginSuccess resLoginSuccess = new ResLoginSuccess();
            BeanUtil.copyProperties(user, resLoginSuccess);
            resLoginSuccess.setToken(token);
            return R.OK(resLoginSuccess);
        }
    }


    public R getOpenId(String appId, String code) {
        if (!this.wxMpService.switchover(appId)) {
            throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的配置，请核实！", appId));
        }
        try {
            WxOAuth2Service wxOAuth2Service=wxMpService.getOAuth2Service();
            WxOAuth2AccessToken oAuth2AccessToken=wxOAuth2Service.getAccessToken(code);
            WxOAuth2UserInfo wxMpUser=wxOAuth2Service.getUserInfo(oAuth2AccessToken,"zh_CN");
            String openid=wxMpUser.getOpenid();
            return R.OK(openid);
        } catch (WxErrorException e) {
            e.printStackTrace();
            return R.ERROR(e.getMessage());
        }
    }

    public R buildAuthorizationUrl(String appId, String url,String scope) {
        if (!this.wxMpService.switchover(appId)) {
            throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的配置，请核实！", appId));
        }
        String returnRul=wxMpService.getOAuth2Service().buildAuthorizationUrl(url, scope, null);
        return R.OK(returnRul);
    }

}
