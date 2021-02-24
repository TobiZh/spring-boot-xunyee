package com.vlinkage.xunyee.entity.response;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("person__check__count")
@Data
public class ResMonPersonCheckCount {

    @Id
    private String id;
    private Integer check;
    private Date data_time;
    private Integer person;
}
