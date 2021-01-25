package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ResSearchHot {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "搜索名称")
    private String name;
}
