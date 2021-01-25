package com.vlinkage.xunyee.entity.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
public class ReqUserInfo {


    @ApiModelProperty("昵称")
    @Length(min = 1,max = 10)
    private String nickname;

    @ApiModelProperty("头像 传绝对路径连接")
    @NotEmpty
    private String avatar;

    @ApiModelProperty("性别")
    private Integer sex;

    @ApiModelProperty("微信city")
    private String wx_city;
    @ApiModelProperty("微信country")
    private String wx_country;
    @ApiModelProperty("微信province")
    private String wx_province;
}
