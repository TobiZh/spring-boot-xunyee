package com.vlinkage.xunyee.entity.request;

import com.vlinkage.xunyee.entity.ReqMyPage;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ReqRecommendPage{

    @ApiModelProperty("动态id")
    private int blog_id;
    @ApiModelProperty("动态类型")
    private int type;
    @ApiModelProperty("动态相关艺人")
    private int person_id;
}
