package com.vlinkage.xunyee.entity.request;


import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Document("vc_user__person__check")
@Data
public class ReqMonUserPersonCheck {

    private int vcuser;
    private int person;
    private int check;
    private LocalDateTime updated;
}
