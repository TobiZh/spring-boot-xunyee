package com.vlinkage.xunyee.entity.response;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ResLoginSuccessMini extends ResLoginSuccessBase {

    @ApiModelProperty("sessionKey")
    private String session_key;
    private String nickname;
    private String avatar;
}
