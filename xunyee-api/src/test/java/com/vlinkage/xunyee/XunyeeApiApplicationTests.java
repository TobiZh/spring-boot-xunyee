package com.vlinkage.xunyee;

import com.alibaba.fastjson.JSONArray;
import com.vlinkage.xunyee.entity.response.ResMonPersonCheckCount;
import com.vlinkage.xunyee.utils.MongodbUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@SpringBootTest
class XunyeeApiApplicationTests {


    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void contextLoads() {

    }

    @Test
    public void mongoTestFind(){
        String[] keys= {"person",};
        Integer[] values={24212};
        List<ResMonPersonCheckCount> all = (List<ResMonPersonCheckCount>) MongodbUtils.find(new ResMonPersonCheckCount(),keys,values,"person__check__count","check");

        System.out.println(JSONArray.toJSON(all));

//        Criteria criteria = Criteria.where("person").is(24212);
//
//        Query query = Query.query(criteria);
//        query.with(Sort.by(Sort.Direction.DESC, "check"));
//        List<ResMonPersonCheckCount> list=mongoTemplate.find(query,ResMonPersonCheckCount.class,"person__check__count");
//        System.out.println(JSONArray.toJSON(list));
    }
}
