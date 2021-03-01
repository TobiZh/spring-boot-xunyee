package com.vlinkage.xunyee.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
public class ResPersonCheckCount {

    @ApiModelProperty("id 等于 personId")
    private int id;
    @ApiModelProperty("签到数量")
    private int check;
    @ApiModelProperty("排名")
    private int rank;
    @ApiModelProperty("当天我为该艺人签到的次数")
    private int check_my;
    @ApiModelProperty("艺人id")
    private int person;
    @ApiModelProperty("艺人名称")
    private String zh_name;
    @ApiModelProperty("艺人头像")
    private String avatar_custom;
    @ApiModelProperty("vcuser_person")
    private String vcuser_person;

}
