package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class ReqPic {


    @ApiModelProperty("是否在小程序中启用")
    private Integer is_enabled_5;

    @ApiModelProperty("是否在app中启用")
    private Integer is_enabled_6;

}
