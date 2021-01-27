package com.vlinkage.xunyee.entity.response;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class ResSystemNotification {

    @ApiModelProperty(value = "自增id")
    private Integer id;

    @ApiModelProperty(value = "接收用户 id 0 发送给所有人")
    private Integer receive_vcuser_id;

    @ApiModelProperty(value = "通知标题")
    private String title;

    @ApiModelProperty(value = "通知内容")
    private String content;

    @ApiModelProperty(value = "状态 0 未读 1已读")
    private Integer is_read;

    @ApiModelProperty(value = "已读时间")
    private Date read_time;

    @ApiModelProperty(value = "创建时间")
    private Date created;
}
