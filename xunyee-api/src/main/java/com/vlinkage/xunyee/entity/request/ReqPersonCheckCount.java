package com.vlinkage.xunyee.entity.request;

import com.vlinkage.xunyee.entity.ReqMyPage;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqPersonCheckCount extends ReqMyPage {

    @ApiModelProperty("按姓名搜索的关键字。为空字符串时接口会返回所有行")
    private String person__zh_name__icontains;

    @ApiModelProperty("数据周期：1为日榜，7为周榜，30为月榜")
    @NotNull
    private Integer period;
}
