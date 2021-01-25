package com.vlinkage.xunyee.api.meta.service;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vlinkage.ant.meta.entity.Brand;
import com.vlinkage.ant.meta.entity.Person;
import com.vlinkage.xunyee.entity.response.ResPerson;
import org.springframework.stereotype.Service;

@Service
public class MetaService {

    @DS("meta")
    public ResPerson getPersonById(int person_id){
        QueryWrapper qw=new QueryWrapper();
        qw.select("zh_name","avatar_custom");
        Person person=new Person().selectById(person_id);
        ResPerson resPerson=BeanUtil.copyProperties(person,ResPerson.class);
        return resPerson;
    }
}
