package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqBlogStar {

    @ApiModelProperty("动态id")
    @NotNull
    private Integer blog_id;
    @ApiModelProperty("类型 0 点踩  1 点赞")
    @NotNull
    private Integer type;
}
