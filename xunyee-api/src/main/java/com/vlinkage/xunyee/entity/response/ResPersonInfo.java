package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ResPersonInfo {

    @ApiModelProperty("艺人id")
    private int person;
    @ApiModelProperty("艺人名称")
    private String zh_name;
    @ApiModelProperty("性别")
    private String sex;
    @ApiModelProperty("艺人头像")
    private String avatar_custom;
    @ApiModelProperty("艺人指数")
    private double report_1912_teleplay;
    @ApiModelProperty("指数排名")
    private int report_1912_teleplay_rank;
    @ApiModelProperty("指数趋势")
    private int report_1912_teleplay_rank_incr;
    @ApiModelProperty("我签到次数 0 未签到")
    private int check_my;
    @ApiModelProperty("今日签到人数")
    private int check;

}
