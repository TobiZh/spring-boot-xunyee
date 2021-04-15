package com.vlinkage.xunyee.api.meta;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vlinkage.ant.meta.entity.Brand;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
@DS("meta")
@Service
public class MetaService {


    @Autowired
    private MyMapper myMapper;


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

    /**
     * 模糊查询有限数量艺人
     * @param name
     * @param limit
     * @return
     */
    public List<ResPerson> getPersonLimit(String name,int limit){

        QueryWrapper qw=new QueryWrapper();
        if (StringUtils.isNotEmpty(name)){
            qw.like("zh_name",name);
        }
        qw.select("id","zh_name","avatar_custom");
        qw.last("limit "+limit);
        List<ResPerson> personList=new Person().selectList(qw);
        List<ResPerson> resPeople=CopyListUtil.copyListProperties(personList,ResPerson.class);
        return resPeople;
    }


    public Person getPersonById(int person_id){
        QueryWrapper qw=new QueryWrapper();
        qw.eq("id",person_id);
        qw.select("id","zh_name","avatar_custom","is_xunyee_check","sex");
        Person person=new Person().selectOne(qw);
        return person;
    }

    public IPage<ResBrandPerson> getBrandPerson(ReqMyPage myPage, int person_id) {
        Page page=new Page(myPage.getCurrent(),myPage.getSize());
        IPage<ResBrandPerson> iPage=myMapper.selectBrandPersonPage(page,person_id);
        iPage.setRecords(CopyListUtil.copyListProperties(iPage.getRecords(),ResBrandPerson.class));
        return iPage;
    }

    /**
     * 查询所有支持签到的艺人
     * 做缓存
     * @return
     */
    @Cacheable(value = "all_check_person")
    public List<Person> getPersonByXunyeeCheck(){
        QueryWrapper qw=new QueryWrapper();
        qw.select("id","zh_name","avatar_custom");
        qw.eq("is_xunyee_check",true);
        List<Person> personList=new Person().selectList(qw);
        return personList;
    }


    public List<Person> getPerson(Integer... ids){
        QueryWrapper qw=new QueryWrapper();
        qw.select("id","zh_name","avatar_custom");
        if (ids.length>0){
            qw.in("id",ids);
        }
        List<Person> personList=new Person().selectList(qw);
        return personList;
    }

    public List<Person> getPersonByName(String name){
        QueryWrapper qw=new QueryWrapper();
        qw.select("id","zh_name","avatar_custom");
        qw.like("zh_name",name);
        List<Person> personList=new Person().selectList(qw);
        return personList;
    }


    /**
     * 获取电视剧
     * @param ids
     * @return
     */
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
    public List<Zy> getZys(Integer... ids){
        QueryWrapper qw=new QueryWrapper();
        qw.select("id","title");
        if (ids.length>0){
            qw.in("id",ids);
        }
        List<Zy> zies=new Zy().selectList(qw);
        return zies;
    }

    /**
     * 获取品牌名称
     * @param brandId
     * @return
     */

    public String getBrandNameById(Integer brandId) {
        Brand brand=new Brand().selectById(brandId);
        if (brand!=null){
            return brand.getName();
        }
       return "";
    }
}
