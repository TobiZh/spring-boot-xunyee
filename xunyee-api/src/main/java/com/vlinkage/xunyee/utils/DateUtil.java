package com.vlinkage.xunyee.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

@Slf4j
public class DateUtil {

    /**
     * 获取一天中剩余的时间（秒数）
     */
    public static Integer getDayRemainingTime(Date currentDate) {
        LocalDateTime midnight = LocalDateTime.ofInstant(currentDate.toInstant(),
                ZoneId.systemDefault()).plusDays(1).withHour(0).withMinute(0)
                .withSecond(0).withNano(0);
        LocalDateTime currentDateTime = LocalDateTime.ofInstant(currentDate.toInstant(),
                ZoneId.systemDefault());
        long seconds = ChronoUnit.SECONDS.between(currentDateTime, midnight);
        return (int) seconds;
    }

    /**
     * date转LocalDateTime
     * @param d
     * @return
     */
    public static LocalDateTime date2LocalDateTime(Date d){
        ZoneId zoneId = ZoneId.systemDefault();
        Instant instant = d.toInstant();
        LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
        return localDateTime;
    }

    /**
     * 获取指定年月的最后一天
     * @param year
     * @param month
     * @return
     */
    public static LocalDate getLastDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        //设置年份
        cal.set(Calendar.YEAR, year);
        //设置月份
        cal.set(Calendar.MONTH, month-1);
        //获取某月最大天数
        int lastDay = cal.getActualMaximum(Calendar.DATE);
        //设置日历中月份的最大天数
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        ZoneId zoneId = ZoneId.systemDefault();
        Instant instant = cal.getTime().toInstant();
        LocalDate localDate = instant.atZone(zoneId).toLocalDate();

        return localDate;
    }


    /**
     * 获取某年第一天日期
     * @param year 年份
     * @return Date
     */
    public static LocalDate getCurrYearFirst(int year){
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        ZoneId zoneId = ZoneId.systemDefault();
        Instant instant = calendar.getTime().toInstant();
        LocalDate localDate = instant.atZone(zoneId).toLocalDate();
        return localDate;
    }

    /**
     * 获取某年最后一天日期
     * @param year 年份
     * @return Date
     */
    public static LocalDate getCurrYearLast(int year){
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.roll(Calendar.DAY_OF_YEAR, -1);
        ZoneId zoneId = ZoneId.systemDefault();
        Instant instant = calendar.getTime().toInstant();
        LocalDate localDate = instant.atZone(zoneId).toLocalDate();
        return localDate;
    }



    public static void main(String[] args) {
        String datestr="2021-09";
        int year= Integer.parseInt(datestr.split("-")[0]);
        int month= Integer.parseInt(datestr.split("-")[1]);

        System.out.println(getLastDayOfMonth(year,month));
        System.out.println(getCurrYearFirst(year));
        System.out.println(getCurrYearLast(year).plusDays(1));
        log.debug("谈恋爱真的很麻烦,所以以后就麻烦你了");
    }
}
