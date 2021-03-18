package com.vlinkage.xunyee.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqMyPage {

    @ApiModelProperty("当前 默认=1")
    @NotNull
    private Integer current = 1;

    @ApiModelProperty("每页显示的数量 默认=20")
    @NotNull
    private Integer size = 20;
}
