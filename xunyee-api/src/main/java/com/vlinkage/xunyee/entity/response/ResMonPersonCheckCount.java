package com.vlinkage.xunyee.entity.response;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("person__check__count")
@Data
public class ResMonPersonCheckCount {
    private Integer id;
    private Integer check;
    private Date data_time;
    private Integer person;
}
