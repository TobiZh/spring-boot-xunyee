package com.vlinkage.xunyee.entity.response;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ResLoginSuccess {

    @ApiModelProperty("用户id")
    private int id;
    @ApiModelProperty("手机号")
    private String phone;
    @ApiModelProperty("头像")
    private String avatar;
    @ApiModelProperty("昵称")
    private String nickname;
    @ApiModelProperty(value = "性别 0 未填写 1 男 2女")
    private Integer gender;
    @ApiModelProperty("openid")
    private String openid;
    @ApiModelProperty("token")
    private String token;

}
