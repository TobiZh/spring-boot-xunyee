package com.vlinkage.xunyee.entity.response;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;

@Document("cache__report__person__rpt_trend")
@Data
public class ResMonReportPersonRptTrend {

    private int period;
    private int person;
    private Double report_1912_teleplay;
    private int report_1912_teleplay_rank;
    private int report_1912_teleplay_rank_incr;
    private Double report_1905_teleplay;
    private int report_1905_teleplay_rank;
    private int report_1905_teleplay_rank_incr;
    private LocalDateTime updated;
}
