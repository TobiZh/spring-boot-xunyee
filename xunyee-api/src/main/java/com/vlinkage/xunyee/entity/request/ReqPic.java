package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class ReqPic {

    @NotNull(message = "必须传 1 封面； 2 轮播图；3 广告 ")
    @ApiModelProperty("类型id：1 封面； 2 轮播图；3 广告")
    private Integer type_id;

    @ApiModelProperty("是否在小程序中启用")
    private Boolean is_enabled_mini;

    @ApiModelProperty("是否在app中启用")
    private Boolean is_enabled_app;

}
