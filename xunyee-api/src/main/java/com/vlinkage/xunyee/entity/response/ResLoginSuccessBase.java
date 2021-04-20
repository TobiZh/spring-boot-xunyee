package com.vlinkage.xunyee.entity.response;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ResLoginSuccessBase {

    @ApiModelProperty("token")
    private String token;

    @ApiModelProperty("refresh_token 当token过期时使用refresh_token 通过 /refresh/token重新获取")
    private String refresh_token;

    @ApiModelProperty("token 有效时间单位秒")
    private Long expires_in;

    @ApiModelProperty("用户id")
    private int vcuser_id;

    @ApiModelProperty("用户昵称")
    private String nickname;

    @ApiModelProperty("用户头像")
    private String avatar;
}
