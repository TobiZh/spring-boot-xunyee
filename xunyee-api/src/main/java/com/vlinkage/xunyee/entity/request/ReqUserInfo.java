package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
public class ReqUserInfo {


    @ApiModelProperty("昵称")
    @Length(max = 10)
    private String nickname;

    @ApiModelProperty("头像 传绝对路径连接")
    private String avatar;

    @ApiModelProperty("性别")
    private Integer sex;

    @ApiModelProperty("个性签名")
    @Length(max = 30)
    private String bio;

    @ApiModelProperty("小程序登录时候使用 微信avatar")
    private String wx_avatar;
    @ApiModelProperty("小程序登录时候使用 微信nickname")
    private String wx_nickname;

    @ApiModelProperty("微信city")
    private String wx_city;
    @ApiModelProperty("微信country")
    private String wx_country;
    @ApiModelProperty("微信province")
    private String wx_province;
}
