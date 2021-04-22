package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ResFollowPage {

    @ApiModelProperty("关注id")
    private int id;
    @ApiModelProperty("用户id")
    private int vcuser_id;
    @ApiModelProperty("用户昵称")
    private String nickname;
    @ApiModelProperty("用户头像")
    private String avatar;
    @ApiModelProperty(value = "类型 1 我的关注'已关注' 我的粉丝 '回关'; 3 互相关注")
    private int type;

    @ApiModelProperty("个人简介")
    private String bio;

}
