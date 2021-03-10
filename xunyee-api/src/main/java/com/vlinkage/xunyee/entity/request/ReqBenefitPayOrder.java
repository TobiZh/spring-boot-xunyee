package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqBenefitPayOrder {


    @ApiModelProperty("6为移动应用，7为网页")
    @NotNull
    private Integer site;
    @ApiModelProperty("权益价格id")
    @NotNull
    private Integer benefit_price;

}
