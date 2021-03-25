package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class ReqUserReport {

    @NotNull
    @ApiModelProperty(value = "被举报人id")
    private Integer report_vcuser_id;

    @ApiModelProperty(value = "举报原因")
    private String title;

    @Length(min = 5)
    @ApiModelProperty(value = "补充说明")
    private String content;

    @ApiModelProperty(value = "相关截图")
    private String images;
}
