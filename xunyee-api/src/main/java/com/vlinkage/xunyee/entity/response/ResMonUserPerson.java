package com.vlinkage.xunyee.entity.response;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("vc_user__person")
@Data
public class ResMonUserPerson {
    private Integer person;
    private LocalDateTime updated;
}
