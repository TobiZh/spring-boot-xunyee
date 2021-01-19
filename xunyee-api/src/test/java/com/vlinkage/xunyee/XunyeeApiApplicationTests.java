package com.vlinkage.xunyee;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vlinkage.ant.xunyee.entity.XunyeeAd;
import com.vlinkage.ant.xunyee.entity.XunyeeVcuser;
import lombok.extern.log4j.Log4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class XunyeeApiApplicationTests {

    @Test
    void contextLoads() {
        QueryWrapper qw=new QueryWrapper();
        qw.last("limit 50");
        List<XunyeeVcuser> ad=new XunyeeVcuser().selectList(qw);
        System.out.println(JSONArray.toJSON(ad));
    }

}
