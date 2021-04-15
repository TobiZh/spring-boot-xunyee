package com.vlinkage.xunyee.entity.response;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ResRefreshToken {

    @ApiModelProperty("token")
    private String token;

    @ApiModelProperty("有效时间单位 秒")
    private int expires_in;

    @ApiModelProperty("refresh_token 当token过期时使用refresh_token 通过 /refresh/token重新获取")
    private String refresh_token;
}
