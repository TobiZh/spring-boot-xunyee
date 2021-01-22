package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


@Data
public class ResNavigation {

    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "icon图标相对路径")
    private String icon;

    @ApiModelProperty(value = "跳转类型 1 跳转webview，2 app内跳转相关页面")
    private Integer type;

    @ApiModelProperty(value = "跳转参数 type=1 是一个url，type=2 是一个参数")
    private String params;


}
