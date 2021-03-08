package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqBlogCategory {

    @ApiModelProperty("动态类型 1 截屏 2 我在现场 3 品牌代言")
    @NotNull
    private Integer type;
}
