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
import com.vlinkage.xunyee.entity.response.ResBrandNameUrl;
import com.vlinkage.xunyee.entity.response.ResBrandPersonList;
import com.vlinkage.xunyee.entity.response.ResPerson;
import com.vlinkage.xunyee.mapper.MyMapper;
import com.vlinkage.xunyee.utils.CopyListUtil;
import com.vlinkage.xunyee.utils.ImageHostUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@DS("meta")
@Service
public class MetaService {


    @Autowired
    private MyMapper myMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ImageHostUtil imageHostUtil;

    public IPage<ResPerson> getPersonPage(ReqMyPage myPage, String name) {

        QueryWrapper qw = new QueryWrapper();
        if (StringUtils.isNotEmpty(name)) {
            qw.like("zh_name", name);
        }
        qw.select("id", "zh_name", "avatar_custom");

        Page page = new Page(myPage.getCurrent(), myPage.getSize());
        IPage<ResPerson> iPage = new Person().selectPage(page, qw);
        List<ResPerson> resPersonList = CopyListUtil.copyListProperties(iPage.getRecords(), ResPerson.class);
        for (ResPerson resPerson : resPersonList) {
            resPerson.setAvatar_custom(imageHostUtil.absImagePath(resPerson.getAvatar_custom()));
        }
        iPage.setRecords(resPersonList);

        return iPage;
    }

    /**
     * 模糊查询有限数量艺人
     *
     * @param name
     * @param limit
     * @return
     */
    public List<ResPerson> getPersonLimit(String name, int limit) {

        QueryWrapper qw = new QueryWrapper();
        if (StringUtils.isNotEmpty(name)) {
            qw.eq("zh_name", name);
        }
        qw.select("id", "zh_name", "avatar_custom");
        qw.orderByDesc("created");
        qw.last("limit " + limit);
        List<ResPerson> personList = new Person().selectList(qw);
        List<ResPerson> resPeoples = CopyListUtil.copyListProperties(personList, ResPerson.class);
        for (ResPerson resPerson : resPeoples) {
            resPerson.setAvatar_custom(imageHostUtil.absImagePath(resPerson.getAvatar_custom()));
        }
        return resPeoples;
    }

    /**
     * 查询某个艺人的头像昵称
     * 做缓存
     *
     * @return
     */
    @Cacheable(value = "springboot_cache_person", key = "#person_id")
    public Person getPersonById(int person_id) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq("id", person_id);
        qw.select("id", "zh_name", "avatar_custom", "is_xunyee_check", "sex");
        Person person = new Person().selectOne(qw);
        person.setAvatar_custom(imageHostUtil.absImagePath(person.getAvatar_custom()));
        return person;
    }


    /**
     * 缓存艺人代言的所有品牌列表
     *
     * @param person_id
     * @return
     */
    @Cacheable(value = "springboot_cache_person_brand", key = "#person_id", unless = "#result == null or #result.size() == 0")
    public List<ResBrandPersonList> getBrandPersonList(int person_id) {
        List<ResBrandPersonList> resBrandPeople = myMapper.selectBrandPersonList(person_id);
        for (ResBrandPersonList resBrandPerson : resBrandPeople) {
            resBrandPerson.setLogo(imageHostUtil.absImagePath(resBrandPerson.getLogo()));
        }
        return resBrandPeople;
    }

    /**
     * 缓存艺人代言的所有品牌列表
     *
     * @param ids
     * @return
     */
    public List<ResBrandPersonList> getBrandByIds(List<Integer> ids) {

        String sql = "SELECT b.id,b.name,b.logo,bps.url_gen url,bps.finish_time_new " +
                "FROM brand b " +
                "LEFT JOIN meta_brand_person bp ON b.id=bp.brand_id " +
                "LEFT JOIN meta_brand_person_site bps ON bp.id=bps.brand_person_id " +
                "WHERE bps.is_enabled=true AND bps.url<>'' and bp.brand_id in (" + StringUtils.join(ids, ",") + ") " +
                "ORDER BY bps.finish_time_new DESC,bps.created DESC";
        List<ResBrandPersonList> resBrandPeople = jdbcTemplate.query(sql, new Object[]{}, new BeanPropertyRowMapper<ResBrandPersonList>(ResBrandPersonList.class));


        //List<ResBrandPersonList> resBrandPeople= myMapper.selectBrandByIds(ids);
        for (ResBrandPersonList resBrandPerson : resBrandPeople) {
            resBrandPerson.setLogo(imageHostUtil.absImagePath(resBrandPerson.getLogo()));
        }
        return resBrandPeople;
    }

    /**
     * 查询所有支持签到的艺人
     * 做缓存
     *
     * @return
     */
    @Cacheable(value = "springboot_cache_check_person")
    public List<Person> getPersonByXunyeeCheck() {
        QueryWrapper qw = new QueryWrapper();
        qw.select("id", "zh_name", "avatar_custom");
        qw.eq("is_xunyee_check", true);
        List<Person> personList = new Person().selectList(qw);
        for (Person person : personList) {
            person.setAvatar_custom(imageHostUtil.absImagePath(person.getAvatar_custom()));
        }
        return personList;
    }


    public List<Person> getPerson(Integer... ids) {
        QueryWrapper qw = new QueryWrapper();
        qw.select("id", "zh_name", "avatar_custom");
        if (ids.length > 0) {
            qw.in("id", ids);
        }
        List<Person> personList = new Person().selectList(qw);
        for (Person person : personList) {
            person.setAvatar_custom(imageHostUtil.absImagePath(person.getAvatar_custom()));
        }
        return personList;
    }

    public List<Person> getPersonByName(String name) {
        QueryWrapper qw = new QueryWrapper();
        qw.select("id", "zh_name", "avatar_custom");
        qw.like("zh_name", name);
        List<Person> personList = new Person().selectList(qw);
        for (Person person : personList) {
            person.setAvatar_custom(imageHostUtil.absImagePath(person.getAvatar_custom()));
        }
        return personList;
    }


    /**
     * 获取电视剧
     *
     * @param ids
     * @return
     */
    public List<Teleplay> getTeleplays(Integer... ids) {
        QueryWrapper qw = new QueryWrapper();
        qw.select("id", "title");
        if (ids.length > 0) {
            qw.in("id", ids);
        }
        List<Teleplay> teleplays = new Teleplay().selectList(qw);
        return teleplays;
    }

    /**
     * 获取综艺
     *
     * @param ids
     * @return
     */
    public List<Zy> getZys(Integer... ids) {
        QueryWrapper qw = new QueryWrapper();
        qw.select("id", "title");
        if (ids.length > 0) {
            qw.in("id", ids);
        }
        List<Zy> zies = new Zy().selectList(qw);
        return zies;
    }

    /**
     * 获取品牌名称
     *
     * @param brandId
     * @return
     */
    public ResBrandNameUrl getBrandNameUrlById(int brandId) {
        ResBrandNameUrl res = myMapper.selectBrandNameUrlById(brandId);
        return res;
    }


}
