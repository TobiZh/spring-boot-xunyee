package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqPersonQuxian {

    @ApiModelProperty("艺人id")
    @NotNull
    private Integer person;

    @ApiModelProperty("是不是80后 0返回全部，1返回80后")
    @NotNull
    private Integer is_eighty;
}
