package com.vlinkage.xunyee.entity.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ResSdbJdSale{

    @ApiModelProperty("开始日期")
    @JsonFormat(pattern = "yyyy.MM.dd")
    private LocalDate start_date;
    @ApiModelProperty("结束日期")
    @JsonFormat(pattern = "yyyy.MM.dd")
    private LocalDate finish_date;

    private List<Rank> rank;

    @Data
    public static class Rank{

        @ApiModelProperty("艺人id")
        private int person;
        @ApiModelProperty("艺人名称")
        private String zh_name;

        // 不明白为什么要这样写 0.938
        private BigDecimal rate;

        // 不明白为什么要这样写 93.8
        private BigDecimal rate_percentage;;
    }


}
