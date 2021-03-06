package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ResPicTitleUrl {

    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("url")
    private String url;
}
