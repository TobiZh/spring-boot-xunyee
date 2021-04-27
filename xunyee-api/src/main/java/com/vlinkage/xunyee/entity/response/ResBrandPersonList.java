package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ResBrandPersonList implements Serializable {

    @ApiModelProperty("品牌id")
    private Integer id;
    @ApiModelProperty("品牌名称")
    private String name;
    @ApiModelProperty("品牌logo")
    private String logo;
    @ApiModelProperty("跳转url")
    private String url;
    @ApiModelProperty("New标志过期时间")
    private Date finish_time_new;
}
