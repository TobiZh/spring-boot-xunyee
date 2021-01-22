package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


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

}
