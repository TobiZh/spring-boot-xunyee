package com.vlinkage.xunyee.api.star.service;

import com.google.common.base.Joiner;
import com.vlinkage.ant.meta.entity.Person;
import com.vlinkage.ant.star.entity.SdbJdSale;
import com.vlinkage.xunyee.api.provide.MetaService;
import com.vlinkage.xunyee.api.provide.StarProvideService;
import com.vlinkage.xunyee.entity.response.*;
import com.vlinkage.xunyee.entity.result.R;
import com.vlinkage.xunyee.utils.ImageHostUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StarService {

    @Autowired
    private StarProvideService starProvideService;

    @Autowired
    private MetaService metaService;

    @Autowired
    private ImageHostUtil imageHostUtil;

    public R<List<ResSdbJdSale>> brandStarRate() {
        List<SdbJdSale> sdbJdSales = starProvideService.getWeekJDSaleRank();
        if (sdbJdSales.size() <= 0) {
            return R.ERROR("数据查询失败");
        }
        // 提取person id去数据库查询电视剧信息
        Integer[] personIds = sdbJdSales.stream().map(e -> e.getPerson_id()).collect(Collectors.toList())
                .toArray(new Integer[sdbJdSales.size()]);
        // 查询数据库艺人信息
        List<Person> persons = metaService.getPerson(personIds);

        // 求和
        BigDecimal volumeSum = new BigDecimal(0);
        for (SdbJdSale sdbJdSale : sdbJdSales) {
            volumeSum = volumeSum.add(sdbJdSale.getSales_volume());
        }

        ResSdbJdSale resSdbJdSale = new ResSdbJdSale();
        LocalDate lastDate = sdbJdSales.get(0).getCreated();
        resSdbJdSale.setStart_date(lastDate.minusDays(6));
        resSdbJdSale.setFinish_date(lastDate);

        List<ResSdbJdSale.Rank> ranks = new ArrayList<>();

        // 防止数据不足10条
        int size = sdbJdSales.size() > 10 ? 10 : sdbJdSales.size();
        BigDecimal lastRate = new BigDecimal(0);
        for (int i = 0; i < size; i++) {
            SdbJdSale sdbJdSale = sdbJdSales.get(i);
            ResSdbJdSale.Rank rank = new ResSdbJdSale.Rank();
            for (Person person : persons) {
                //这里使用equals 比较 Integer
                if (sdbJdSale.getPerson_id().equals(person.getId())) {
                    rank.setPerson(person.getId());
                    rank.setZh_name(person.getZh_name());
                    BigDecimal rate = sdbJdSale.getSales_volume().divide(volumeSum, 3, BigDecimal.ROUND_HALF_UP);
                    lastRate = lastRate.add(rate);
                    rank.setRate(rate);
                    rank.setRate_percentage(rate.multiply(BigDecimal.valueOf(100)));
                    break;
                }
            }
            ranks.add(rank);
        }
        ResSdbJdSale.Rank rank = new ResSdbJdSale.Rank();
        rank.setPerson(0);
        rank.setZh_name("其他");
        BigDecimal rate = BigDecimal.valueOf(1).subtract(lastRate);
        rank.setRate(rate);
        rank.setRate_percentage(rate.multiply(BigDecimal.valueOf(100)));
        ranks.add(rank);
        resSdbJdSale.setRank(ranks);
        return R.OK(resSdbJdSale);
    }

    public R<ResSdbJdInfo> brandStarInfo() {
        List<SdbJdSale> sdbJdSales = starProvideService.getWeekJDSaleRank();
        if (sdbJdSales.size() <= 0) {
            return R.ERROR("数据查询失败");
        }
        // 提取person id去数据库查询电视剧信息
        Integer[] personIds = sdbJdSales.stream().map(e -> e.getPerson_id()).collect(Collectors.toList())
                .toArray(new Integer[sdbJdSales.size()]);
        // 查询数据库艺人信息
        List<Person> persons = metaService.getPerson(personIds);
        // 组装数据 组装艺人信息 艺人关联品牌
        List<ResSdbJdInfo.Person> personRanks = new ArrayList<>();
        int i = 0;
        for (SdbJdSale sdbJdSale : sdbJdSales) {
            if (i >= 3) {
                break;
            }
            ResSdbJdInfo.Person personRank = new ResSdbJdInfo.Person();
            for (Person person : persons) {
                //这里使用equals 比较 Integer
                if (sdbJdSale.getPerson_id().equals(person.getId())) {
                    i++;
                    personRank.setPerson(person.getId());
                    personRank.setAvatar(imageHostUtil.absImagePath(person.getAvatar_custom()));
                    personRank.setZh_name(person.getZh_name());
                    personRanks.add(personRank);
                }
            }
        }
        ResSdbJdInfo resSdbJdInfo = new ResSdbJdInfo();
        // 开始时间结束时间
        LocalDate lastDate = sdbJdSales.get(0).getCreated();
        resSdbJdInfo.setStart_date(lastDate.minusDays(6));
        resSdbJdInfo.setFinish_date(lastDate);
        // 艺人前三头像
        resSdbJdInfo.setPerson(personRanks);
        return R.OK(resSdbJdInfo);

    }

    public R<List<ResSdbJdPersonBrand>> brandStarRank(String name) {
        // 组装数据 组装艺人信息 艺人关联品牌
        List<ResSdbJdPersonBrand> ranks = new ArrayList<>();

        if (StringUtils.isEmpty(name)) {
            List<SdbJdSale> sdbJdSales = starProvideService.getWeekJDSaleRank();
            if (sdbJdSales.size() <= 0) {
                return R.ERROR("搜索结果为空");
            }
            // 提取person id去数据库查询电视剧信息
            Integer[] personIds = sdbJdSales.stream().map(e -> e.getPerson_id()).collect(Collectors.toList())
                    .toArray(new Integer[sdbJdSales.size()]);
            // 查询数据库艺人信息
            List<Person> persons = metaService.getPerson(personIds);
            if (persons.size()<=0){
                return R.ERROR("搜索结果为空");
            }
            // 查询艺人代言的品牌
            List<ResBrandPersonList> brandPersonLists = metaService.getPersonBrandListByPersonIds(Joiner.on(",").join(personIds));


            for (SdbJdSale sdbJdSale : sdbJdSales) {
                ResSdbJdPersonBrand rank = new ResSdbJdPersonBrand();
                // 组装 艺人代言的品牌
                List<ResBrandPersonList> brands = new ArrayList<>();
                for (Person person : persons) {
                    //这里使用equals 比较 Integer
                    if (sdbJdSale.getPerson_id().equals(person.getId())) {
                        rank.setPerson(person.getId());
                        rank.setZh_name(person.getZh_name());
                        rank.setAvatar(imageHostUtil.absImagePath(person.getAvatar_custom()));
                        rank.setBrands(brands);
                        ranks.add(rank);
                        break;
                    }
                }
            }
            for (ResSdbJdPersonBrand rank : ranks) {
                for (ResBrandPersonList brandPersonList : brandPersonLists) {
                    if (rank.getPerson() == brandPersonList.getPerson_id()) {
                        rank.getBrands().add(brandPersonList);
                    }
                }
            }
            return R.OK(ranks);

        } else {
            // 查询数据库艺人信息
            List<ResPerson> persons = metaService.getPersonsBrandByPersonName(name);
            if (persons.size()<=0){
                return R.ERROR("搜索结果为空");
            }
            // 提取person id去数据库查询电视剧信息
            Integer[] personIds = persons.stream().map(e -> e.getId()).collect(Collectors.toList())
                    .toArray(new Integer[persons.size()]);
            // 查询艺人代言的品牌
            List<ResBrandPersonList> brandPersonLists = metaService.getPersonBrandListByPersonIds(Joiner.on(",").join(personIds));
            // 组装 艺人代言的品牌
            for (ResPerson person : persons) {
                List<ResBrandPersonList> brands = new ArrayList<>();
                ResSdbJdPersonBrand rank=new ResSdbJdPersonBrand();
                rank.setPerson(person.getId());
                rank.setAvatar(person.getAvatar_custom());
                rank.setZh_name(person.getZh_name());
                rank.setBrands(brands);
                ranks.add(rank);
            }

            for (ResSdbJdPersonBrand rank : ranks) {
                for (ResBrandPersonList brand : brandPersonLists) {
                    if (rank.getPerson() == brand.getPerson_id()) {
                        rank.getBrands().add(brand);
                    }
                }
            }

            return R.OK(ranks);
        }
    }
}
