package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqBlogId {

    @ApiModelProperty("动态id")
    @NotNull
    private Integer blog_id;
}
