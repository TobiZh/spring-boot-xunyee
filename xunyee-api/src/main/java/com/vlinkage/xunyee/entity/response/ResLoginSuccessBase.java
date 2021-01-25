package com.vlinkage.xunyee.entity.response;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ResLoginSuccessBase {

    @ApiModelProperty("token")
    private String token;

}
