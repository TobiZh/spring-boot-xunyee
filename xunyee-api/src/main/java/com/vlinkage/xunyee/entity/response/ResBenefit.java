package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ResBenefit {

    @ApiModelProperty("开始时间")
    private LocalDate start_time;
    @ApiModelProperty("结束时间")
    private LocalDate finish_time;
}
