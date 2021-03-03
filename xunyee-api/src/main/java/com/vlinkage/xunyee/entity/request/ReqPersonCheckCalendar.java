package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NonNull;

@Data
public class ReqPersonCheckCalendar {

    @NonNull
    @ApiModelProperty("艺人id")
    private Integer person;

    @ApiModelProperty("日期 例如：2020-09")
    private String data_date;
}
