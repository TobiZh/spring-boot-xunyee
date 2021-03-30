package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@Data
public class ResReportPersonRptTrendQux {

    @ApiModelProperty("艺人id")
    private int person;
    @ApiModelProperty("指数")
    private Double report_1912_teleplay;
    @ApiModelProperty("排名")
    private int report_1912_teleplay_rank;
    @ApiModelProperty("数据日期 2021-01-01")
    private Date data_date;


}
