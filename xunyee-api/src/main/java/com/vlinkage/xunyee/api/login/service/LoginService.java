package com.vlinkage.xunyee.api.login.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.vlinkage.common.entity.result.R;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class LoginService {

    @Resource
    private RedisUtil redisUtil;

    @Autowired
    private WxMpService wxMpService;


    @Transactional
    public R wxOpenLogin(String appId,ThirdLoginParam loginParam) {
        if (!this.wxMpService.switchover(appId)) {
            throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的配置，请核实！", appId));
        }

        try {

            WxMpOAuth2AccessToken oAuth2AccessToken=wxMpService.getOAuth2Service().getAccessToken(loginParam.getCode());
            WxMpUser wxMpUser=wxMpService.getOAuth2Service().getUserInfo(oAuth2AccessToken,"zh_CN");
            String unionid= wxMpUser.getUnionId();
            String openid=wxMpUser.getOpenId();
            QueryWrapper qw = new QueryWrapper();
            qw.eq("unionid", unionid);

            TmpUserThird temp = new TmpUserThird().selectOne(qw);
            if (temp == null) {
                // 新增账号
                TmpUser user = new TmpUser();
                user.setNickname(wxMpUser.getNickname());
                user.setAvatar(wxMpUser.getHeadImgUrl());
                user.setOpenid(openid);
                user.setWxCity(wxMpUser.getCity());
                user.setWxCountry(wxMpUser.getCountry());
                user.setWxProvince(wxMpUser.getProvince());
                user.setGender(wxMpUser.getSex());
                user.setUnionid(unionid);
                if (user.insert()) {
                    // 新增第三方账号
                    TmpUserThird userThird = new TmpUserThird();
                    userThird.setThirdType(1);
                    userThird.setOpenid(openid);
                    if (StringUtils.isNotEmpty(unionid)) {
                        userThird.setUnionid(unionid);
                    }
                    userThird.setUserId(user.getId());
                    userThird.insert();

                    // 登录成功 生成token
                    String token = JwtUtil.getToken(String.valueOf(user.getId()));
                    //写入token
                    user.setToken(token);
                    user.updateById();
                    ResLoginSuccess resLoginSuccess = new ResLoginSuccess();
                    BeanUtil.copyProperties(user, resLoginSuccess);
                    resLoginSuccess.setToken(token);
                    return R.OK(resLoginSuccess);
                }

                return R.ERROR();

            } else {

                TmpUser user = new TmpUser().selectById(temp.getUserId());

                if(!temp.getOpenid().equals(openid)){
                    // h5或者小程序登录的时候如果openid不一致将openid更新
                    temp.setOpenid(openid);
                    temp.updateById();
                    user.setOpenid(openid);
                }
                // 登录成功 生成token
                String token = JwtUtil.getToken(String.valueOf(user.getId()));
                //写入token
                user.setToken(token);
                user.updateById();

                ResLoginSuccess resLoginSuccess = new ResLoginSuccess();
                BeanUtil.copyProperties(user, resLoginSuccess);
                resLoginSuccess.setToken(token);
                return R.OK(resLoginSuccess);
            }
        } catch (WxErrorException e) {
            e.printStackTrace();
            return R.ERROR(e.getMessage());
        }
    }

    public R getOpenId(String appId, String code) {
        if (!this.wxMpService.switchover(appId)) {
            throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的配置，请核实！", appId));
        }
        try {
            WxMpOAuth2AccessToken oAuth2AccessToken = wxMpService.getOAuth2Service().getAccessToken(code);
            WxMpUser wxMpUser=wxMpService.getOAuth2Service().getUserInfo(oAuth2AccessToken,"zh_CN");
            String openid=wxMpUser.getOpenId();
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
