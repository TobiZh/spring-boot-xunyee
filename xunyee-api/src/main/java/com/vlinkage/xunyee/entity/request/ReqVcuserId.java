package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqVcuserId {

    @ApiModelProperty("对方的用户id")
    @NotNull
    private Integer vcuser_id;
}
