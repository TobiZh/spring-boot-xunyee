package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqBlogCategory {

    @ApiModelProperty("动态类型 0 关注 1 推荐 2 截屏 3 现场热拍 4 品牌代言")
    @NotNull
    private Integer type;
}
