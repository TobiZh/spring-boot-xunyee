package com.vlinkage.xunyee.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqMyPage {

    @NotNull
    @ApiModelProperty("当前 默认=1")
    private Integer current = 1;

    @NotNull
    @ApiModelProperty("每页显示的数量 默认=20")
    private Integer size = 20;
}
