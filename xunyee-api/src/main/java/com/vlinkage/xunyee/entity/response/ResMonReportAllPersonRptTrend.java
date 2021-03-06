package com.vlinkage.xunyee.entity.response;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("cache__report__all_person__rpt_trend")
@Data
public class ResMonReportAllPersonRptTrend {

    private int period;
    private int person;
    private Double report_1912_teleplay;
    private int report_1912_teleplay_rank;
    private int report_1912_teleplay_rank_incr;
    private Double report_1905_teleplay;
    private int report_1905_teleplay_rank;
    private int report_1905_teleplay_rank_incr;
    private LocalDateTime start_data_time;
    private LocalDateTime finish_data_time;
    private LocalDateTime updated;
}
