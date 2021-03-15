package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqGlobalSearch {

    @ApiModelProperty("搜索关键字")
    @NotNull
    private String name;
}
