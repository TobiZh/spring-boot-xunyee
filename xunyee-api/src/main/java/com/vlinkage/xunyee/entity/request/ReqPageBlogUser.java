package com.vlinkage.xunyee.entity.request;

import com.vlinkage.xunyee.entity.ReqMyPage;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ReqPageBlogUser extends ReqMyPage {

    @ApiModelProperty("不传表示查询当前登录用户的")
    private Integer vcuserId;
}
