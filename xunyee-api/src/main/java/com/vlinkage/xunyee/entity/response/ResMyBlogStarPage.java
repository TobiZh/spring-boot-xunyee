package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ResMyBlogStarPage {

    @ApiModelProperty("用户id")
    private int vcuser_id;
    @ApiModelProperty("头像")
    private String avatar;
    @ApiModelProperty("昵称")
    private String nickname;
    @ApiModelProperty("图片list")
    private String cover;
    @ApiModelProperty("图片list")
    private Boolean is_star;
}
