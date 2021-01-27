package com.vlinkage.xunyee.entity.request;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NonNull;
import org.hibernate.validator.constraints.Length;


@Data
public class ReqFeedback {

    @NonNull
    @ApiModelProperty(value = "举报原因")
    private String title;

    @Length(min = 5,max = 140)
    @ApiModelProperty(value = "补充说明")
    private String content;

    @ApiModelProperty(value = "相关截图")
    private String images;

}
