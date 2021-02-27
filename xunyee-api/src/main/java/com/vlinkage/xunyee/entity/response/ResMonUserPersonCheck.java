package com.vlinkage.xunyee.entity.response;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("vc_user__person__check")
@Data
public class ResMonUserPersonCheck {
    private Integer person;
    private Integer check;
}
