package com.vlinkage.xunyee.entity.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ResRank<T> implements Serializable {
    @ApiModelProperty("总数据")
    private int count;
    @ApiModelProperty("总页数")
    private int pages;
    @ApiModelProperty("当前第几页")
    private int current;
    @ApiModelProperty("榜单开始时间")
    @JsonFormat(pattern = "yyyy.MM.dd",timezone="GMT+8")
    private LocalDate data_time__gte;
    @ApiModelProperty("榜单结束时间")
    @JsonFormat(pattern = "yyyy.MM.dd",timezone="GMT+8")
    private LocalDate data_time__lte;
    @ApiModelProperty("系统时间")
    private LocalDateTime systime;
    @ApiModelProperty("倒计时")
    private long today_reamin_second;
    @ApiModelProperty("榜单数据列表")
    private T results;
}
