package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ResReportTeleplayNetRptTrend {

    @ApiModelProperty("请求类型 1 ，7 ，30")
    private int period;
    @ApiModelProperty("电视剧id")
    private int teleplay;
    private Double report_1905;
    private int report_1905_rank;
    private int report_1905_rank_incr;
    private TeleplayFK teleplay_fk;

    @Data
    public static class TeleplayFK{
        @ApiModelProperty("电视剧id")
        private int id;
        @ApiModelProperty("电视剧标题")
        private String title;
    }
}
