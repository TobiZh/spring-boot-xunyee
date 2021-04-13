package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NonNull;

@Data
public class ReqPersonCheck {

    @NonNull
    @ApiModelProperty("艺人id")
    private Integer person;
}
