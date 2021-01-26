package com.vlinkage.xunyee.entity.request;

import com.vlinkage.xunyee.entity.ReqMyPage;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqPageFollow extends ReqMyPage {

    @NotNull
    @ApiModelProperty("1 我的关注 2 我的粉丝")
    private Integer type;
}
