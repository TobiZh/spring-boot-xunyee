package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ResPerson {

    @ApiModelProperty("艺人id")
    private Integer id;
    @ApiModelProperty("艺人名称")
    private String zh_name;
    @ApiModelProperty("艺人头像")
    private String avatar_custom;
}
