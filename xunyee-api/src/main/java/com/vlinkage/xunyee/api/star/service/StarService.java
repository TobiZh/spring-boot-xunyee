package com.vlinkage.xunyee.api.star.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vlinkage.ant.star.entity.PersonGallery;
import com.vlinkage.xunyee.entity.ReqMyPage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StarService {

    @Value("${sys-config.image-path}")
    private String imagePath;


    @DS("star")
    public IPage getPersonGalleryByPersonId(int person, ReqMyPage myPage){

        QueryWrapper qw=new QueryWrapper();
        qw.select("original");
        qw.eq("person_id",person);
        qw.eq("disabled",2);//枚举类型 enum("true","false")
        qw.orderByAsc("orderby");
        Page page=new Page(myPage.getCurrent(),myPage.getSize());
        IPage<PersonGallery> iPage= new PersonGallery().selectPage(page,qw);

        return iPage;
    }
}
