package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ResXunyeeBenefitPrice {

    @ApiModelProperty("id")
    private int id;
    @ApiModelProperty("price")
    private BigDecimal price;
    @ApiModelProperty("tag_price")
    private BigDecimal tag_price;
    @ApiModelProperty("benefit_id")
    private int benefit_id;
    @ApiModelProperty("是该权益价格的天数。比如月卡的quantity为30，季卡的的quantity为90")
    private int quantity;
}
