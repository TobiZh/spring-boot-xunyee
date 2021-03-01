package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ResReportZyRptTrend {

    @ApiModelProperty("请求类型 1 ，7 ，30")
    private int period;
    @ApiModelProperty("综艺Id")
    private int zy;
    private Double report_1905;
    private int report_1905_rank;
    private int report_1905_rank_incr;
    private ZyFK zy_fk;

    @Data
    public static class ZyFK{
        @ApiModelProperty("电视剧id")
        private int id;
        @ApiModelProperty("电视剧标题")
        private String title;
    }
}
