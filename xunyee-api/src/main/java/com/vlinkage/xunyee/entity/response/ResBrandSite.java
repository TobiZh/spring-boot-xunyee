package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ResBrandSite {

    @ApiModelProperty("品牌id")
    private Integer id;
    @ApiModelProperty("品牌名称")
    private String name;
    @ApiModelProperty("品牌logo")
    private String logo;
    @ApiModelProperty("跳转链接")
    private String url;
}
