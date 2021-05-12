package com.vlinkage.xunyee;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vlinkage.ant.xunyee.entity.XunyeeVcuserBenefit;
import com.vlinkage.xunyee.entity.response.ResMonUserPersonCheck;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = XunyeeApiApplication.class)
class XunyeeApiApplicationTests {

    @Autowired
    private MongoTemplate mongoTemplate;

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

    @Test
    public void mongo(){
        Aggregation aggregation=Aggregation.newAggregation(
                Aggregation.project().and("updated").dateAsFormattedString("%Y-%m-%d").as("updated"),
                Aggregation.match(Criteria.where("vcuser").is(3358279)),
                Aggregation.group("updated").count().as("person")
        );
        AggregationResults<ResMonUserPersonCheck> res=mongoTemplate.aggregate(aggregation,"vc_user__person__check",ResMonUserPersonCheck.class);
        System.out.println(JSONObject.toJSONString(res));
    }

}
