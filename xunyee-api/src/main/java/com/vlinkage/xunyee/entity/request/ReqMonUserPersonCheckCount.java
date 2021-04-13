package com.vlinkage.xunyee.entity.request;


import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;


@Document("vc_user__person__check__count")
@Data
public class ReqMonUserPersonCheckCount {

    private int vcuser;
    private int person;
    private int check;
    private LocalDateTime updated;
    private int year;
}
