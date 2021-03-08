package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqVoucher {

    @ApiModelProperty("兑换码")
    @NotNull
    private String voucher;
}
