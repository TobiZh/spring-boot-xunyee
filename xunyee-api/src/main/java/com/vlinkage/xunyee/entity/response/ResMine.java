package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@Data
public class ResMine {
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
    @ApiModelProperty("我的爱豆数量")
    private int idol_count;
    @ApiModelProperty("我的爱豆头像")
    private List<String> persons;

}
