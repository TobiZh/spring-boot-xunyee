package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqRecommendPage{

    @ApiModelProperty("动态id")
    @NotNull
    private Integer blog_id;

    @ApiModelProperty("动态类型")
    @NotNull
    private Integer type;

    @ApiModelProperty("动态相关艺人")
    @NotNull
    private Integer person_id;
}
