package com.vlinkage.xunyee.entity.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


@Data
public class ResPic {

    @ApiModelProperty("id")
    private Long id;
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("描述")
    private String memo;
    @ApiModelProperty("图片")
    private String pic;
    @ApiModelProperty("url")
    private String url;
    @ApiModelProperty("type_id")
    private Integer type_id;
    @ApiModelProperty("开始时间")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date start_time;
    @ApiModelProperty("结束时间")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date finish_time;
    @ApiModelProperty("排序ASC")
    private Integer sequence;
    @ApiModelProperty("是否在小程序开启")
    private Boolean is_enabled_5;
    @ApiModelProperty("是否在app开启")
    private Boolean is_enabled_6;

}
