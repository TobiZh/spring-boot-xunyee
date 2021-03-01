package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NonNull;

@Data
public class ReqUserPersonCheck {


    @NonNull
    @ApiModelProperty("艺人id")
    private Integer person;
    @NonNull
    @ApiModelProperty("是否关注： 0 不关注 ，1 关注")
    private Integer is_enabled;
}
