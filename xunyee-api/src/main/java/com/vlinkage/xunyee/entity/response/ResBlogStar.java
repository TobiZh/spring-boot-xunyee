package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 点赞返回
 */
@Data
public class ResBlogStar {
    @ApiModelProperty("动态id blog_id")
    private int id;
    @ApiModelProperty("是否点赞")
    private Boolean is_star=false;
    @ApiModelProperty("点赞数量")
    private int star_count;
    @ApiModelProperty("是否点踩")
    private Boolean is_unstar=false;
    @ApiModelProperty("点踩数量")
    private int unstar_count;
}
