package com.vlinkage.xunyee.entity.response;



import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class ResBlogInfo {

    @ApiModelProperty("用户id")
    private Integer vcuser_id;
    @ApiModelProperty("用户昵称")
    private String nickname;
    @ApiModelProperty("用户头像")
    private String avatar;

    @ApiModelProperty("发布时间")
    private Date created;

    @ApiModelProperty("0 关注 1 已关注 2 回关 3 互相关注")
    private int follow_type;

    @ApiModelProperty("动态id")
    private Integer blog_id;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("图片 用英文,分割")
    private String images;

    @ApiModelProperty("动态类型 1 截屏 2 我在现场 3 品牌代言")
    private int type;

    @ApiModelProperty("类型 id")
    private Integer type_id;

    @ApiModelProperty("艺人id")
    private int person_id;

    @ApiModelProperty("艺人名称")
    private String person_name;

    @ApiModelProperty("艺人头像")
    private String person_avatar_customer;

    @ApiModelProperty("点赞数量")
    private int star_count;
    @ApiModelProperty("点踩数量")
    private int unstar_count;
    @ApiModelProperty("收藏数量")
    private int favorite_count;

    @ApiModelProperty("是否点赞")
    private Boolean is_star = false;
    @ApiModelProperty("是否点踩")
    private Boolean is_unstar = false;
    @ApiModelProperty("是否收藏")
    private Boolean is_favorite = false;



}
