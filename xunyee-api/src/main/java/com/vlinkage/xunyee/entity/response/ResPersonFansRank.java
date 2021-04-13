package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ResPersonFansRank {

    @ApiModelProperty("当前年份")
    private int year;
    @ApiModelProperty("截止日期")
    private LocalDate end_date;
    @ApiModelProperty("真爱粉丝列表最多40个")
    private List<Fans> fans;

    @Data
    public static class Fans{
        @ApiModelProperty("用户id")
        public int vcuser_id;
        @ApiModelProperty("用户头像")
        public String avatar;
        @ApiModelProperty("用户昵称")
        public String nickname;
        @ApiModelProperty("签到次数")
        public int check;

    }
}
