package com.vlinkage.xunyee.entity.request;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ReqAd {

    @ApiModelProperty("广告id")
    @NotNull(message = "id不能为空")
    private String id;

    @NotNull(message = "标题不能为空")
    private String title;

}
