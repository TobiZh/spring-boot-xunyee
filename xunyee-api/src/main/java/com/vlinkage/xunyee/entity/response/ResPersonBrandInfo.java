package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ResPersonBrandInfo {

    @ApiModelProperty("点击量，当前版本去掉了，都是返回0")
    private int click;
    @ApiModelProperty("带货排行，-1表示没有带货排行")
    private int sale_rank;
    @ApiModelProperty("艺人关联的品牌")
    private List<ResBrandPersonList> brand_list;
}
