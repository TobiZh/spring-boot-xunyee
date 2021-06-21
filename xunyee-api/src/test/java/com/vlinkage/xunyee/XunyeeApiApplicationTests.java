package com.vlinkage.xunyee;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vlinkage.ant.xunyee.entity.XunyeeVcuserBenefit;
import com.vlinkage.xunyee.entity.response.ResMonUserPersonCheck;
import com.vlinkage.xunyee.utils.DateUtil;
import com.vlinkage.xunyee.utils.ImageHostUtil;
import com.vlinkage.xunyee.utils.JsonUtils;
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

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = XunyeeApiApplication.class)
class XunyeeApiApplicationTests {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ImageHostUtil imageHostUtil;


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
    public void mongo() {
        Aggregation aggregation=Aggregation.newAggregation(
                Aggregation.match(Criteria.where("vcuser").is(3358279).and("updated").gte(DateUtil.getCurrYearFirst(LocalDate.now().getYear()))),
                Aggregation.group("person").count().as("check")
        );
        AggregationResults<ResMonUserPersonCheck> res=mongoTemplate.aggregate(aggregation,"vc_user__person__check",ResMonUserPersonCheck.class);
        System.out.println(JsonUtils.objectToJson(res.getMappedResults()));
    }

    @Test
    public void mongoCheckDays() {
        Aggregation aggregation=Aggregation.newAggregation(
                Aggregation.match(Criteria.where("vcuser").is(19).and("updated").gte(DateUtil.getCurrYearFirst(LocalDate.now().getYear()))),
                Aggregation.project("merchno", "amount")
                        .andExpression("{ $dateToString:{format:'%Y-%m-%d',date: '$updated',timezone: 'Asia/Shanghai' }}").as("date"),
                Aggregation.group("date").count().as("check")
        );

        AggregationResults<ResMonUserPersonCheck> res=mongoTemplate.aggregate(aggregation,"vc_user__person__check",ResMonUserPersonCheck.class);
        System.out.println(JsonUtils.objectToJson(res.getMappedResults().size()));
    }


    @Test
    public void host(){
        System.out.println(imageHostUtil.absImagePath("http://blog"));
        System.out.println(imageHostUtil.removeImagePath("https://img.xunyee.cn/test.jpg"));
    }


    @Test
    public void uuidInsertTest(){
        XunyeeVcuserBenefit vcuserBenefit = new XunyeeVcuserBenefit();
        vcuserBenefit.setId(UUID.randomUUID().toString());//@TableId(value = "id", type = IdType.INPUT)
        vcuserBenefit.setBenefit_id(1);
        vcuserBenefit.setVcuser_id(3753216);
        vcuserBenefit.setUpdated(new Date());
        vcuserBenefit.setCreated(new Date());
        vcuserBenefit.setStart_time(LocalDate.now());
        vcuserBenefit.setFinish_time(LocalDate.now().plusDays(30));
        vcuserBenefit.insert();
    }


}
