package com.vlinkage.xunyee.entity.response;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("cache__report__netteleplay__rpt_trend")
@Data
public class ResMonReportTeleplayNetRptTrend {

    private int period;
    private int teleplay;
    private Double report_1905;
    private int report_1905_rank;
    private int report_1905_rank_incr;
}
