package com.vlinkage.xunyee;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vlinkage.ant.xunyee.entity.XunyeeVcuserBenefit;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = XunyeeApiApplication.class)
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
