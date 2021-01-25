package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class ReqBlogReport {

    @NotNull
    @ApiModelProperty(value = "创作id")
    private Integer blog_id;

    @NotNull
    @ApiModelProperty(value = "举报人id")
    private Integer vcuser_id;

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
