package com.vlinkage.xunyee.entity.response;



import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class ResBlogInfo {

    @ApiModelProperty("用户id")
    private Integer vcuserId;
    @ApiModelProperty("用户昵称")
    private String nickname;
    @ApiModelProperty("用户头像")
    private String avatar;
    @ApiModelProperty("发布时间")
    private Date created;
    @ApiModelProperty("关注 回关 互相关注 取关")
    private int followType;

    @ApiModelProperty("动态id")
    private Integer blogId;
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("内容")
    private String content;
    @ApiModelProperty("图片 用英文,分割")
    private String images;
    @ApiModelProperty("动态类型 1 截屏 2 我在现场 3 品牌代言")
    private int type;
    @ApiModelProperty("类型 id")
    private Integer typeId;
    @ApiModelProperty("艺人id")
    private int personId;
    @ApiModelProperty("艺人名称")
    private String personName;
    @ApiModelProperty("品牌名称")
    private String brandName;
    @ApiModelProperty("点赞数量")
    private int starCount;
    @ApiModelProperty("点踩数量")
    private int unstarCount;
    @ApiModelProperty("收藏数量")
    private int favoriteCount;

    @ApiModelProperty("是否点赞")
    private Boolean isStar = false;
    @ApiModelProperty("是否点踩")
    private Boolean isUnstar = false;
    @ApiModelProperty("是否收藏")
    private Boolean isFavorite = false;



}
