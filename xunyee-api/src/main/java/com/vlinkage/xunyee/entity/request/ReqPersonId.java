package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqPersonId {

    @ApiModelProperty("艺人id")
    @NotNull
    private Integer person;
}
