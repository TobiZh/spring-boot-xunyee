package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ResBlogPage {

    @ApiModelProperty("创作id")
    private int id;
    @ApiModelProperty("用户id")
    private int vcuser_d;
    @ApiModelProperty("用户昵称")
    private String nickname;
    @ApiModelProperty("用户头像")
    private String avatar;
    @ApiModelProperty("封面图地址")
    private String cover;
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("点赞数量")
    private int star_count;
    @ApiModelProperty("是否点赞")
    private Boolean is_star = false;

}
