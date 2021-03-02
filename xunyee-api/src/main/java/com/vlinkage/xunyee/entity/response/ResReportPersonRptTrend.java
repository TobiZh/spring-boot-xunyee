package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

public class ResReportPersonRptTrend {
    @ApiModelProperty("请求类型 1 ，7 ，30")
    private int period;
    @ApiModelProperty("艺人id")
    private int person;
    private Double report_1912_teleplay;
    private int report_1912_teleplay_rank;
    private int report_1912_teleplay_rank_incr;
    private Double report_1905_teleplay;
    private int report_1905_teleplay_rank;
    private int report_1905_teleplay_rank_incr;
    private PersonFK person_fk;

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getPerson() {
        return person;
    }

    public void setPerson(int person) {
        this.person = person;
    }

    public Double getReport_1912_teleplay() {
        return report_1912_teleplay;
    }

    public void setReport_1912_teleplay(Double report_1912_teleplay) {
        this.report_1912_teleplay = report_1912_teleplay;
    }

    public int getReport_1912_teleplay_rank() {
        return report_1912_teleplay_rank;
    }

    public void setReport_1912_teleplay_rank(int report_1912_teleplay_rank) {
        this.report_1912_teleplay_rank = report_1912_teleplay_rank;
    }

    public int getReport_1912_teleplay_rank_incr() {
        return report_1912_teleplay_rank_incr;
    }

    public void setReport_1912_teleplay_rank_incr(int report_1912_teleplay_rank_incr) {
        this.report_1912_teleplay_rank_incr = report_1912_teleplay_rank_incr;
    }

    public Double getReport_1905_teleplay() {
        return report_1912_teleplay;
    }

    public void setReport_1905_teleplay(Double report_1905_teleplay) {
        this.report_1905_teleplay = report_1905_teleplay;
    }

    public int getReport_1905_teleplay_rank() {
        return report_1912_teleplay_rank;
    }

    public void setReport_1905_teleplay_rank(int report_1905_teleplay_rank) {
        this.report_1905_teleplay_rank = report_1905_teleplay_rank;
    }

    public int getReport_1905_teleplay_rank_incr() {
        return report_1912_teleplay_rank_incr;
    }

    public void setReport_1905_teleplay_rank_incr(int report_1905_teleplay_rank_incr) {
        this.report_1905_teleplay_rank_incr = report_1905_teleplay_rank_incr;
    }

    public PersonFK getPerson_fk() {
        return person_fk;
    }

    public void setPerson_fk(PersonFK person_fk) {
        this.person_fk = person_fk;
    }

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
