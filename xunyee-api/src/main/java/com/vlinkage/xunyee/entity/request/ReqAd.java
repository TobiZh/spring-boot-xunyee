package com.vlinkage.xunyee.entity.request;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class ReqAd {

    @ApiModelProperty("广告id")
    private int id;

}
