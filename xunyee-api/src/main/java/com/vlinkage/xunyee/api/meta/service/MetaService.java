package com.vlinkage.xunyee.api.meta.service;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vlinkage.ant.meta.entity.Person;
import com.vlinkage.ant.meta.entity.Teleplay;
import com.vlinkage.ant.meta.entity.Zy;
import com.vlinkage.xunyee.entity.ReqMyPage;
import com.vlinkage.xunyee.entity.response.ResBrandPerson;
import com.vlinkage.xunyee.entity.response.ResPerson;
import com.vlinkage.xunyee.mapper.MyMapper;
import com.vlinkage.xunyee.utils.CopyListUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetaService {


    @Autowired
    private MyMapper myMapper;


    @DS("meta")
    public IPage<ResPerson> getPersonPage(ReqMyPage myPage,String name){

        QueryWrapper qw=new QueryWrapper();
        if (StringUtils.isNotEmpty(name)){
            qw.like("zh_name",name);
        }
        qw.select("id","zh_name","avatar_custom");

        Page page=new Page(myPage.getCurrent(),myPage.getSize());
        IPage<ResPerson> iPage=new Person().selectPage(page,qw);
        iPage.setRecords(CopyListUtil.copyListProperties(iPage.getRecords(),ResPerson.class));
        return iPage;
    }

    @DS("meta")
    public ResPerson getPersonById(int person_id){
        QueryWrapper qw=new QueryWrapper();
        qw.eq("id",person_id);
        qw.select("id","zh_name","avatar_custom");
        Person person=new Person().selectOne(qw);
        ResPerson resPerson=BeanUtil.copyProperties(person,ResPerson.class);
        return resPerson;
    }

    @DS("meta")
    public IPage<ResBrandPerson> getBrandPerson(ReqMyPage myPage, int person_id) {
        Page page=new Page(myPage.getCurrent(),myPage.getSize());
        IPage<ResBrandPerson> iPage=myMapper.selectBrandPersonPage(page,person_id);
        iPage.setRecords(CopyListUtil.copyListProperties(iPage.getRecords(),ResBrandPerson.class));
        return iPage;
    }

    @DS("meta")
    public List<Person> getPersonCheck(Integer... ids){
        QueryWrapper qw=new QueryWrapper();
        qw.select("id","zh_name","avatar_custom");
        qw.eq("is_xunyee_check",true);
        if (ids.length>0){
            qw.in("id",ids);
        }
        List<Person> personList=new Person().selectList(qw);
        return personList;
    }


    /**
     * 获取电视剧
     * @param ids
     * @return
     */
    @DS("meta")
    public List<Teleplay> getTeleplays(Integer... ids){
        QueryWrapper qw=new QueryWrapper();
        qw.select("id","title");
        if (ids.length>0){
            qw.in("id",ids);
        }
        List<Teleplay> teleplays=new Teleplay().selectList(qw);
        return teleplays;
    }

    /**
     * 获取综艺
     * @param ids
     * @return
     */
    @DS("meta")
    public List<Zy> getZys(Integer... ids){
        QueryWrapper qw=new QueryWrapper();
        qw.select("id","title");
        if (ids.length>0){
            qw.in("id",ids);
        }
        List<Zy> zies=new Zy().selectList(qw);
        return zies;
    }
}
