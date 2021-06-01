package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class ReqVoucher {

    @ApiModelProperty("兑换码")
    @Size(min = 17,max = 20,message = "兑换码填写错误")
    private String voucher;
}
