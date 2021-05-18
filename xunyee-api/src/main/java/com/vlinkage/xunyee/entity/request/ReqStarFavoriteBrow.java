package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqStarFavoriteBrow {

    @ApiModelProperty("1 点赞 2 收藏 3 历史浏览")
    @NotNull
    private Integer type;
}
