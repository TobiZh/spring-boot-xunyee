package com.vlinkage.xunyee.entity.request;


import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class ReqAd {

    @Size(min = 1 ,max = 10,message = "id 1-10")
    private int id;

}
