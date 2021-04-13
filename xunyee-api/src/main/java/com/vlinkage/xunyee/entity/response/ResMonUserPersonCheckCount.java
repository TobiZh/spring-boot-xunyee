package com.vlinkage.xunyee.entity.response;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("vc_user__person__check__count")
public class ResMonUserPersonCheckCount {

    private int vcuser;
    private int check;

}
