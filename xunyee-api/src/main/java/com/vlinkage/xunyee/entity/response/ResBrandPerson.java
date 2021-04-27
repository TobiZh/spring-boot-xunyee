package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ResBrandPerson {

    @ApiModelProperty("品牌id")
    private Integer id;
    @ApiModelProperty("品牌名称")
    private String name;
    @ApiModelProperty("品牌logo")
    private String logo;
}
