package com.vlinkage.xunyee.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "qiniu")
public class QiNiuProperties {

//    accessKey: pMl4it5o_EuifiT-PRkhjQXJAHqJqktEIQfmxrpK
//    secretKey: vffUicFA7RXCJdov5Ow6f-9z6UKy09LYgv60TdIA
//    bucket: xunyee-blog
//    path: https://up.qiniup.com  # (华东) 每个地区的地址都不一样
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String path;


}
