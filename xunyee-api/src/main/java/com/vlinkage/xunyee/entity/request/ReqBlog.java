package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class ReqBlog {

    @NotNull
    @ApiModelProperty(value = "艺人id")
    private Integer person_id;

    @Length(max = 10)
    @ApiModelProperty(value = "标题")
    private String title;

    @NotEmpty
    @ApiModelProperty(value = "图片地址多张 用英文逗号隔开")
    private String images;

    @Length(min = 5)
    @ApiModelProperty(value = "内容至少5个字符")
    private String content;

    @NotNull
    @ApiModelProperty(value = "动态类型 1 截屏 2 我在现场 3 品牌代言")
    private Integer type;

    @ApiModelProperty(value = "类型 id")
    private Integer type_id;
}
