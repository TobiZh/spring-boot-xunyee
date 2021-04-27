package com.vlinkage.xunyee.api.star.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vlinkage.ant.star.entity.SdbJdSale;
import com.vlinkage.ant.star.entity.SdbPersonGallery;
import com.vlinkage.xunyee.entity.ReqMyPage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@DS("star")
@Service
public class StarService {

    public IPage getPersonGalleryByPersonId(int person, ReqMyPage myPage){

        QueryWrapper qw=new QueryWrapper();
        qw.select("original");
        qw.eq("person_id",person);
        qw.eq("disabled",2);//枚举类型 enum("true","false")
        qw.orderByAsc("orderby");
        Page page=new Page(myPage.getCurrent(),myPage.getSize());
        IPage<SdbPersonGallery> iPage= new SdbPersonGallery().selectPage(page,qw);

        return iPage;
    }


    /**
     * 获取某个艺人带货排行
     * @param person
     * @return
     */
    public int getJDSaleRankByPerson(int person){

        QueryWrapper qw=new QueryWrapper();
        qw.eq("person_id",person);
        qw.orderByDesc("created");
        qw.last("limit 1");
        SdbJdSale jdSale=new SdbJdSale().selectOne(qw);
        if (jdSale==null){
            return -1;
        }else{
            return jdSale.getRank();
        }
    }
}
