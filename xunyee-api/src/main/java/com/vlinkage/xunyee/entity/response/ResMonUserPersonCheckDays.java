package com.vlinkage.xunyee.entity.response;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("vc_user__person__check")
@Data
public class ResMonUserPersonCheckDays {
    private Integer id;
    private Integer check;
}
