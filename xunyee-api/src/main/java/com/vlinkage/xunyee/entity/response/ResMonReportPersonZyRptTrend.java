package com.vlinkage.xunyee.entity.response;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("cache__report__z_person__rpt_trend")
@Data
public class ResMonReportPersonZyRptTrend {

    private int period;
    private int person;
    private Double report_1912_zy;
    private int report_1912_zy_rank;
    private int report_1912_zy_rank_incr;
    private Double report_1905_zy;
    private int report_1905_zy_rank;
    private int report_1905_zy_rank_incr;
}
