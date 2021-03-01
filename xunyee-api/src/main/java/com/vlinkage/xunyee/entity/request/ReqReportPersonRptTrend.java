package com.vlinkage.xunyee.entity.request;

import com.vlinkage.xunyee.entity.ReqMyPage;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ReqReportPersonRptTrend extends ReqMyPage {

    @ApiModelProperty("搜索关键字")
    private String person__zh_name__icontains;
    @ApiModelProperty("数据周期：1为日榜，7为周榜，30为月榜")
    private int period;
}
