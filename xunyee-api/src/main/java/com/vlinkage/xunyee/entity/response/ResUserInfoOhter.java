package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ResUserInfoOhter {
    @ApiModelProperty("用户id")
    private int vcuser_id;
    @ApiModelProperty("用户头像")
    private String avatar;
    @ApiModelProperty("用户昵称")
    private String nickname;
    @ApiModelProperty("关注数量")
    private int follow_count;
    @ApiModelProperty("粉丝数量")
    private int fans_count;
    @ApiModelProperty("点赞数量")
    private int star_count;

    @ApiModelProperty("个性签名")
    private String bio;

    @ApiModelProperty("爱豆数量")
    private int idol_count;

    @ApiModelProperty("签到天数")
    private int check_days_count;

    @ApiModelProperty("是不是会员")
    private Boolean is_vip;

    @ApiModelProperty("关注类型 0 未关注， 1 已关注，2 回关，3 互相关注")
    private int follow_type;

    @ApiModelProperty("共同关注")
    private List<String> persons;
}
