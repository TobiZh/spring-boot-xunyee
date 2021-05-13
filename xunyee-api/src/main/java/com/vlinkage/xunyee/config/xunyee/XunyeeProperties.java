package com.vlinkage.xunyee.config.xunyee;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "xunyee.config")
public class XunyeeProperties {

    private String host;
    private String imageHost;
}
