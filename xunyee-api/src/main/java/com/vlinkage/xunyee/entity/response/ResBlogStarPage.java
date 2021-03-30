package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class ResBlogStarPage {

    @ApiModelProperty("用户id")
    private int vcuser_id;

    @ApiModelProperty("头像")
    private String avatar;
    @ApiModelProperty("昵称")
    private String nickname;
    @ApiModelProperty("是否会员")
    private Boolean is_vip=false;
    @ApiModelProperty("点赞时间")
    private Date created;
    @ApiModelProperty("内容")
    private String content;
    @ApiModelProperty("图片")
    private String images;

}
