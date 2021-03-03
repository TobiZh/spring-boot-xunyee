package com.vlinkage.xunyee.entity.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("vc_user__person__check")
@Data
public class ResMonUserPersonCheckCalendar {
    private Integer check;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date updated;
}
