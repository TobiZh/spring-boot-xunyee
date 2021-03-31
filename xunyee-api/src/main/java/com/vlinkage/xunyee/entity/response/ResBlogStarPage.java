package com.vlinkage.xunyee.entity.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import jdk.nashorn.internal.ir.annotations.Ignore;
import lombok.Data;

import java.util.Date;
import java.util.List;

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
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String images;
    @ApiModelProperty("图片list")
    private List<String> image_list;
}
