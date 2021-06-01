package com.vlinkage.xunyee.entity.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ResSdbJdSaleRank {

    @ApiModelProperty("开始日期")
    @JsonFormat(pattern = "yyyy.MM.dd")
    private LocalDate start_date;
    @ApiModelProperty("结束日期")
    @JsonFormat(pattern = "yyyy.MM.dd")
    private LocalDate finish_date;

    @Data
    public static class Rank{
        @ApiModelProperty("艺人id")
        private int person;
        @ApiModelProperty("艺人名称")
        private String zh_name;
        @ApiModelProperty("艺人头像")
        private String avatar;
        @ApiModelProperty("品牌列表")
        private List<ResBrandPersonList> brands;
    }
}
