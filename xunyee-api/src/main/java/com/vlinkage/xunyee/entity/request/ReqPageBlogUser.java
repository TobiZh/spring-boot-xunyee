package com.vlinkage.xunyee.entity.request;

import com.vlinkage.xunyee.entity.ReqMyPage;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqPageBlogUser extends ReqMyPage {

    @ApiModelProperty("用户id")
    @NotNull
    private Integer vcuser_id;
}
