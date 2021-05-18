package com.vlinkage.xunyee.entity.response;



import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ResBlogInfo {

    @ApiModelProperty("用户id")
    private Integer vcuser_id;
    @ApiModelProperty("用户昵称")
    private String nickname;
    @ApiModelProperty("用户头像")
    private String avatar;

    @ApiModelProperty("发布时间 8月19日17:30 ")
    @JsonFormat(pattern="MM月dd日HH:mm",timezone = "GMT+8")
    private Date created;

    @ApiModelProperty("0 关注 1 回关 2 已关注 3 互相关注")
    private int follow_type;

    @ApiModelProperty("动态id")
    private Integer blog_id;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("图片list")
    private List<String> image_list;

    @ApiModelProperty("动态类型 1 截屏 2 我在现场 3 品牌代言")
    private int type;

    @ApiModelProperty("类型 id")
    private Integer type_id;

    @ApiModelProperty("品牌名称 当type=3的时候才有值")
    private String brand_name;

    @ApiModelProperty("品牌跳转链接 当type=3的时候才有值")
    private String brand_url;

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
