package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ResAd {

    @ApiModelProperty("广告id")
    private int id;
    @ApiModelProperty("标题")
    private String title;
}
