package com.vlinkage.xunyee.entity.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


@Data
public class ResUserPersonCheckCalendar {

    @ApiModelProperty("当前月签到天数")
    private int count;
    private CheckCount check_count;
    private DateData date_data;
    private List<Result> results;
    public static class Result{
        private Integer check;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date date;
        private int date__year;
        private int date__month;
        private int date__day;

        public Integer getCheck() {
            return check;
        }

        public void setCheck(Integer check) {
            this.check = check;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            this.date__year=year;
            this.date__month=month;
            this.date__day=day;
        }

        public int getDate__year() {
            return date__year;
        }

        public void setDate__year(int date__year) {
            this.date__year = date__year;
        }

        public int getDate__month() {
            return date__month;
        }

        public void setDate__month(int date__month) {
            this.date__month = date__month;
        }

        public int getDate__day() {
            return date__day;
        }

        public void setDate__day(int date__day) {
            this.date__day = date__day;
        }
    }




    @Data
    public static class CheckCount{
        @ApiModelProperty("本月签到次数")
        private int month;
        @ApiModelProperty("今年签到次数")
        private int year;
    }


    public static class DateData{
        private LocalDate date__gte;
        private int date__gte__year;
        private int date__gte__month;
        private int date__gte__day;
        private LocalDate date__lte;
        private int date__lte__year;
        private int date__lte__month;
        private int date__lte__day;

        public LocalDate getDate__gte() {
            return date__gte;
        }

        public void setDate__gte(LocalDate date__gte) {
            this.date__gte = date__gte;
            int year = date__gte.getYear();
            int month =date__gte.getMonthValue();
            int day = date__gte.getDayOfMonth();
            this.date__gte__year=year;
            this.date__gte__month=month;
            this.date__gte__day=day;
        }

        public int getDate__gte__year() {
            return date__gte__year;
        }

        public void setDate__gte__year(int date__gte__year) {
            this.date__gte__year = date__gte__year;
        }

        public int getDate__gte__month() {
            return date__gte__month;
        }

        public void setDate__gte__month(int date__gte__month) {
            this.date__gte__month = date__gte__month;
        }

        public int getDate__gte__day() {
            return date__gte__day;
        }

        public void setDate__gte__day(int date__gte__day) {
            this.date__gte__day = date__gte__day;
        }

        public LocalDate getDate__lte() {
            return date__lte;
        }

        public void setDate__lte(LocalDate date__lte) {
            this.date__lte = date__lte;
            int year = date__lte.getYear();
            int month =date__lte.getMonthValue();
            int day = date__lte.getDayOfMonth();
            this.date__lte__year=year;
            this.date__lte__month=month;
            this.date__lte__day=day;
        }

        public int getDate__lte__year() {
            return date__lte__year;
        }

        public void setDate__lte__year(int date__lte__year) {
            this.date__lte__year = date__lte__year;
        }

        public int getDate__lte__month() {
            return date__lte__month;
        }

        public void setDate__lte__month(int date__lte__month) {
            this.date__lte__month = date__lte__month;
        }

        public int getDate__lte__day() {
            return date__lte__day;
        }

        public void setDate__lte__day(int date__lte__day) {
            this.date__lte__day = date__lte__day;
        }
    }


}
