package com.vlinkage.xunyee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(value = "com.vlinkage")
@SpringBootApplication
public class XunyeeApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(XunyeeApiApplication.class, args);
    }

}
