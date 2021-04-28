package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class ResAppVersion {

    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("标题 2.0.2版本已更新")
    private String title;
    @ApiModelProperty("版本名称 2.0.2")
    private String version_name;
    @ApiModelProperty("版本号 202")
    private Integer version_code;
    @ApiModelProperty("更新内容富文本 <p>1.修复bug</p>")
    private String update_message;
    @ApiModelProperty("是否强制更新")
    private Boolean is_force;
    @ApiModelProperty("apk下载地址")
    private String apk_download_url;
    @ApiModelProperty("更新时间")
    private Date created;


}
