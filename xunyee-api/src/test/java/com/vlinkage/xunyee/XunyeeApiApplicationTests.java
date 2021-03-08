package com.vlinkage.xunyee;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vlinkage.ant.xunyee.entity.XunyeeBenefitPrice;
import com.vlinkage.ant.xunyee.entity.XunyeeVcuserBenefit;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@SpringBootTest
class XunyeeApiApplicationTests {


    @Test
    void contextLoads() {

    }

    @Test
    public void mongoTestFind(){

        QueryWrapper qw=new QueryWrapper();
        qw.eq("id", UUID.fromString("48f6e0f6-aab2-40a0-b2e6-5ce6a5147165"));
        XunyeeVcuserBenefit temp=new XunyeeVcuserBenefit().selectOne(qw);
        System.out.println(temp);
    }
}
