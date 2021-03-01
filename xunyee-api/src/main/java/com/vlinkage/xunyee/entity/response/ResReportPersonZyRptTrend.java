package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ResReportPersonZyRptTrend {
    @ApiModelProperty("请求类型 1 ，7 ，30")
    private int period;
    @ApiModelProperty("艺人id")
    private int person;
    private Double report_1912_zy;
    private int report_1912_zy_rank;
    private int report_1912_zy_rank_incr;
    private Double report_1905_zy;
    private int report_1905_zy_rank;
    private int report_1905_zy_rank_incr;
    private PersonFK person_fk;

    @Data
    public static class PersonFK{
        @ApiModelProperty("艺人id")
        private int id;
        @ApiModelProperty("艺人名称")
        private String zh_name;
        @ApiModelProperty("艺人头像")
        private String avatar_custom;
    }
}
