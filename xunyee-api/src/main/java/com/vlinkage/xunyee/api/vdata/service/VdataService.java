package com.vlinkage.xunyee.api.vdata.service;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vlinkage.ant.meta.entity.Person;
import com.vlinkage.ant.meta.entity.Teleplay;
import com.vlinkage.ant.meta.entity.Zy;
import com.vlinkage.xunyee.entity.request.ReqPersonCheckCount;
import com.vlinkage.xunyee.entity.result.R;
import com.vlinkage.xunyee.api.provide.MetaService;
import com.vlinkage.xunyee.entity.request.ReqReportPersonRptTrend;
import com.vlinkage.xunyee.entity.request.ReqReportTeleplayRptTrend;
import com.vlinkage.xunyee.entity.request.ReqReportZyRptTrend;
import com.vlinkage.xunyee.entity.response.*;
import com.vlinkage.xunyee.utils.DateUtil;
import com.vlinkage.xunyee.utils.ImageHostUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VdataService {

    @Autowired
    private ImageHostUtil imageHostUtil;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MetaService metaService;

    public R<ResRank<ResPersonCheckCount>> personCheckCount(Integer userId, ReqPersonCheckCount req) {

        int period = req.getPeriod();
        int current = req.getCurrent();
        int size = req.getSize();
        int rankStart = 1; // 分页rank起始值

        LocalDate nowDate = LocalDate.now();//今天
        LocalDate gteDate;
        LocalDate ltDate;
        if (period <= 1) {//获取今天签到榜
            gteDate = nowDate;
            ltDate = gteDate.plusDays(1);
        } else {
            gteDate = nowDate.minusDays(period);// 减去 7||30
            ltDate = nowDate;
        }

        // 查询条件
        Criteria criteria = Criteria.where("data_time").gte(gteDate).lt(ltDate);
        // 根据person分组 check 求和
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("person").sum("check").as("check"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "check"))
        );
        AggregationResults<ResMonPersonCheckCount> outputTypeCount = mongoTemplate.aggregate(aggregation, "person__check__count",
                ResMonPersonCheckCount.class);
        // 查询mongo中有签到数据的艺人
        List<ResMonPersonCheckCount> resMgs = outputTypeCount.getMappedResults();

        // 查询所有的可签到艺人 大概500个
        List<Person> persons = metaService.getPersonByXunyeeCheck();
        // 查询记录总数 数据总页数
        int totalCount = persons.size();
        int totalPage = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;
        // 查询当前用户关注的艺人和当天签到数
        List<ResMonUserPersonCheck> userPersonChecks = period <= 1 && userId != null ?
                mongoTemplate.find(new Query(Criteria.where("vcuser").is(userId)
                                .andOperator(Criteria.where("updated").gte(gteDate).lt(ltDate))),
                        ResMonUserPersonCheck.class) : new ArrayList<>();

        // 组装数据
        List<ResPersonCheckCount> resCheckCounts = new ArrayList<>();
        for (Person p : persons) {
            int personId = p.getId();
            ResPersonCheckCount resPersonCheckCount = new ResPersonCheckCount();
            resPersonCheckCount.setPerson(personId);
            resPersonCheckCount.setId(personId);
            resPersonCheckCount.setVcuser_person("");//不知道是什么参数
            resPersonCheckCount.setAvatar_custom(imageHostUtil.absImagePath(p.getAvatar_custom()));
            resPersonCheckCount.setZh_name(p.getZh_name());

            //-------------------- 当前用户>>艺人签到数 --------------------
            if (period <= 1) {
                for (ResMonUserPersonCheck userPersonCheck : userPersonChecks) {
                    if (personId == userPersonCheck.getPerson()) {
                        resPersonCheckCount.setCheck_my(userPersonCheck.getCheck());
                        break;
                    }
                }
            }
            //-------------------- 当前用户>>艺人签到数 --------------------

            //-------------------- 当前艺人签到数 --------------------
            for (int i = 0; i < resMgs.size(); i++) {
                ResMonPersonCheckCount mon = resMgs.get(i);
                Integer tmpPerson = mon.getId();
                if (personId == tmpPerson) {
                    resPersonCheckCount.setCheck(mon.getCheck());
                    break;
                }
            }
            //-------------------- 当前艺人签到数 --------------------
            resCheckCounts.add(resPersonCheckCount);
        }

        // 排序
        List<ResPersonCheckCount> resSortCheckCounts = resCheckCounts.stream()
                .sorted(Comparator.comparing(ResPersonCheckCount::getCheck).reversed())// 按签到数 倒叙
                .collect(Collectors.toList());
        // rank
        for (int i = 0; i < resSortCheckCounts.size(); i++) {
            resSortCheckCounts.get(i).setRank(rankStart + i);
        }
        if (StringUtils.isNotEmpty(req.getPerson__zh_name__icontains())) {
            resSortCheckCounts = resCheckCounts.stream()
                    .filter(checkCount -> checkCount.getZh_name().contains(req.getPerson__zh_name__icontains()))
                    .sorted(Comparator.comparing(ResPersonCheckCount::getCheck).reversed())// 按签到数 倒叙
                    .collect(Collectors.toList());

            // 搜索的总数和页数
            totalCount = resSortCheckCounts.size();
            totalPage = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;
        } else {
            resSortCheckCounts = resCheckCounts.stream()
                    .sorted(Comparator.comparing(ResPersonCheckCount::getCheck).reversed())// 按签到数 倒叙
                    .collect(Collectors.toList());
        }

        // 分页
        List<ResPersonCheckCount> pageList = resSortCheckCounts.stream().skip((current - 1) * size).limit(size).collect(Collectors.toList());


        ResRank resRank = new ResRank();
        resRank.setCount(totalCount);
        resRank.setPages(totalPage);
        resRank.setCurrent(current);
        resRank.setData_time__gte(gteDate);
        resRank.setData_time__lte(period > 1 ? nowDate.minusDays(1) : nowDate);
        resRank.setSystime(LocalDateTime.now());
        resRank.setToday_reamin_second(DateUtil.getDayRemainingTime(new Date()));
        resRank.setResults(pageList);
        return R.OK(resRank);
    }

    public R<IPage<ResRank>> reportPersonRptTrend(ReqReportPersonRptTrend req) {
        int period = req.getPeriod();
        int current = req.getCurrent();
        int size = req.getSize();
        LocalDate gteDate=getGteDate(period);
        LocalDate lteDate=getYesterdayLtDate();
        // count的查询条件
        Criteria criteriaCount = Criteria.where("period").is(period);
        Query queryCount = new Query();

        // 查询条件
        Criteria criteria = Criteria.where("period").is(period);
        Query query = new Query();
        List<Person> persons;
        List<ResMonReportPersonRptTrend> resMonReportPersonRptTrends;
        if (StringUtils.isNotEmpty(req.getPerson__zh_name__icontains())){ // 根据艺人名字搜索
            persons=metaService.getPersonByName(req.getPerson__zh_name__icontains());
            Integer[] person = persons.stream().map(e -> e.getId()).collect(Collectors.toList())
                    .toArray(new Integer[persons.size()]);
            criteria.and("person").in(person);
            query.addCriteria(criteria);

            criteriaCount.and("person").in(person);


            query.skip((current - 1) * size).limit(size);
            query.with(Sort.by(Sort.Direction.ASC, "report_1912_teleplay_rank"));
            resMonReportPersonRptTrends=mongoTemplate.find(query,ResMonReportPersonRptTrend.class);

        }else{
            query.addCriteria(criteria);
            query.skip((current - 1) * size).limit(size);
            query.with(Sort.by(Sort.Direction.ASC, "report_1912_teleplay_rank"));
            resMonReportPersonRptTrends=mongoTemplate.find(query,ResMonReportPersonRptTrend.class);
            // 提取person id去数据库查询艺人信息
            Integer[] personIds = resMonReportPersonRptTrends.stream().map(e -> e.getPerson()).collect(Collectors.toList())
                    .toArray(new Integer[resMonReportPersonRptTrends.size()]);
            persons=personIds.length>0?metaService.getPerson(personIds):new ArrayList<>();
        }

        // 查询记录总数 数据总页数 放在分页条件之前

        queryCount.addCriteria(criteriaCount);
        int totalCount =(int) mongoTemplate.count(queryCount, ResMonReportPersonRptTrend.class);
        int totalPage = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;

        // 组装数据
        List<ResReportPersonRptTrend> resTrends = new ArrayList<>();
        for (int i = 0; i < resMonReportPersonRptTrends.size(); i++) {
            ResMonReportPersonRptTrend mon = resMonReportPersonRptTrends.get(i);
            Integer personId = Integer.valueOf(mon.getPerson());
            ResReportPersonRptTrend resReportPersonRptTrend = BeanUtil.copyProperties(mon,ResReportPersonRptTrend.class);

            //-------------------- 当前艺人头像昵称 --------------------
            ResReportPersonRptTrend.PersonFK personFK=new ResReportPersonRptTrend.PersonFK();
            for (Person p : persons) {
                int tmpPerson = p.getId();
                if (personId == tmpPerson) {
                    personFK.setId(personId);
                    personFK.setAvatar_custom(imageHostUtil.absImagePath(p.getAvatar_custom()));
                    personFK.setZh_name(p.getZh_name());
                }
            }
            //-------------------- 当前艺人头像昵称 --------------------
            resReportPersonRptTrend.setPerson_fk(personFK);

            resTrends.add(resReportPersonRptTrend);
        }

        return rank(totalCount,totalPage,current,gteDate,lteDate,resTrends);

    }

    public R<IPage<ResRank>> reportPersonRptTrendZy(ReqReportPersonRptTrend req) {
        int period = req.getPeriod();
        int current = req.getCurrent();
        int size = req.getSize();
        LocalDate gteDate=getGteDate(period);
        LocalDate lteDate=getYesterdayLtDate();


        // 查询条件
        Criteria criteria = Criteria.where("period").is(period);
        Query query = new Query();
        // 查询记录总数 数据总页数 放在分页条件之前
        int totalCount = 0;
        List<Person> persons;
        List<ResMonReportPersonZyRptTrend> resMongos;

        if (StringUtils.isNotEmpty(req.getPerson__zh_name__icontains())){ // 根据艺人名字搜索
            persons=metaService.getPersonByName(req.getPerson__zh_name__icontains());
            Integer[] person = persons.stream().map(e -> e.getId()).collect(Collectors.toList())
                    .toArray(new Integer[persons.size()]);
            criteria.and("person").in(person);
            query.addCriteria(criteria);
            totalCount=(int) mongoTemplate.count(query, ResMonReportPersonZyRptTrend.class);

            query.skip((current - 1) * size).limit(size);
            query.with(Sort.by(Sort.Direction.ASC, "report_1912_zy_rank"));
            resMongos=mongoTemplate.find(query,ResMonReportPersonZyRptTrend.class);

        }else{
            query.addCriteria(criteria);
            totalCount=(int) mongoTemplate.count(query, ResMonReportPersonZyRptTrend.class);

            query.skip((current - 1) * size).limit(size);
            query.with(Sort.by(Sort.Direction.ASC, "report_1912_zy_rank"));
            resMongos=mongoTemplate.find(query,ResMonReportPersonZyRptTrend.class);
            // 提取person id去数据库查询艺人信息
            Integer[] personIds = resMongos.stream().map(e -> e.getPerson()).collect(Collectors.toList())
                    .toArray(new Integer[resMongos.size()]);
            persons=personIds.length>0?metaService.getPerson(personIds):new ArrayList<>();
        }

        int totalPage = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;


        // 组装数据
        List<ResReportPersonZyRptTrend> resTrends = new ArrayList<>();
        for (int i = 0; i < resMongos.size(); i++) {
            ResMonReportPersonZyRptTrend mon = resMongos.get(i);
            Integer personId = mon.getPerson();
            ResReportPersonZyRptTrend resTrend = BeanUtil.copyProperties(mon,ResReportPersonZyRptTrend.class);

            //-------------------- 当前艺人头像昵称 --------------------
            ResReportPersonZyRptTrend.PersonFK personFK=new ResReportPersonZyRptTrend.PersonFK();
            for (Person p : persons) {
                int tmpPerson = p.getId();
                if (personId == tmpPerson) {
                    personFK.setId(personId);
                    personFK.setAvatar_custom(imageHostUtil.absImagePath(p.getAvatar_custom()));
                    personFK.setZh_name(p.getZh_name());
                }
            }
            //-------------------- 当前艺人头像昵称 --------------------
            resTrend.setPerson_fk(personFK);

            resTrends.add(resTrend);
        }

        return rank(totalCount,totalPage,current,gteDate,lteDate,resTrends);
    }

    public R<IPage<ResRank>> reportTeleplayRptTrend(ReqReportTeleplayRptTrend req) {
        int period = req.getPeriod();
        int current = req.getCurrent();
        int size = req.getSize();
        LocalDate gteDate=getGteDate(period);
        LocalDate lteDate=getYesterdayLtDate();

        // 查询条件
        Criteria criteria = Criteria.where("period").is(period);
        Query query = Query.query(criteria);
        // 查询记录总数 数据总页数 放在分页条件之前
        int totalCount = (int) mongoTemplate.count(query, ResMonReportTeleplayRptTrend.class);
        int totalPage = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;

        // 设置起始数
        query.skip((current - 1) * size).limit(size);
        query.with(Sort.by(Sort.Direction.ASC, "report_1905_rank"));
        List<ResMonReportTeleplayRptTrend> resMonReportPersonRptTrends=mongoTemplate.find(query, ResMonReportTeleplayRptTrend.class);
        // 提取teleplay id去数据库查询电视剧信息
        Integer[] teleplayIds = resMonReportPersonRptTrends.stream().map(e -> e.getTeleplay()).collect(Collectors.toList())
                .toArray(new Integer[resMonReportPersonRptTrends.size()]);
        List<Teleplay> teleplays=teleplayIds.length>0?metaService.getTeleplays(teleplayIds):new ArrayList<>();

        // 组装数据
        List<ResReportTeleplayRptTrend> resTrends = new ArrayList<>();
        for (int i = 0; i < resMonReportPersonRptTrends.size(); i++) {
            ResMonReportTeleplayRptTrend mon = resMonReportPersonRptTrends.get(i);
            Integer teleplayId = Integer.valueOf(mon.getTeleplay());
            ResReportTeleplayRptTrend resReportTeleplayRptTrend = BeanUtil.copyProperties(mon,ResReportTeleplayRptTrend.class);

            //-------------------- 当前电视剧标题 --------------------
            ResReportTeleplayRptTrend.TeleplayFK teleplayFK=new ResReportTeleplayRptTrend.TeleplayFK();
            for (Teleplay t : teleplays) {
                int tmpPerson = t.getId();
                if (teleplayId == tmpPerson) {
                    teleplayFK.setId(teleplayId);
                    teleplayFK.setTitle(t.getTitle());
                }
            }
            //-------------------- 当前电视剧标题 --------------------
            resReportTeleplayRptTrend.setTeleplay_fk(teleplayFK);

            resTrends.add(resReportTeleplayRptTrend);
        }

        return rank(totalCount,totalPage,current,gteDate,lteDate,resTrends);
    }

    public R<IPage<ResRank>> reportTeleplayRptTrendNet(ReqReportTeleplayRptTrend req) {
        int period = req.getPeriod();
        int current = req.getCurrent();
        int size = req.getSize();
        LocalDate gteDate=getGteDate(period);
        LocalDate lteDate=getYesterdayLtDate();

        // 查询条件
        Criteria criteria = Criteria.where("period").is(period);
        Query query = Query.query(criteria);
        // 查询记录总数 数据总页数 放在分页条件之前
        int totalCount = (int) mongoTemplate.count(query, ResMonReportTeleplayNetRptTrend.class);
        int totalPage = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;

        // 设置起始数
        query.skip((current - 1) * size)
                // 设置查询条数
                .limit(size);
        query.with(Sort.by(Sort.Direction.ASC, "report_1905_rank"));
        List<ResMonReportTeleplayNetRptTrend> resMonReportTeleplayNetRptTrends=mongoTemplate.find(query, ResMonReportTeleplayNetRptTrend.class);
        // 提取teleplay id去数据库查询电视剧信息
        Integer[] teleplayIds = resMonReportTeleplayNetRptTrends.stream().map(e -> e.getTeleplay()).collect(Collectors.toList())
                .toArray(new Integer[resMonReportTeleplayNetRptTrends.size()]);
        List<Teleplay> teleplays=teleplayIds.length>0?metaService.getTeleplays(teleplayIds):new ArrayList<>();

        // 组装数据
        List<ResReportTeleplayNetRptTrend> resTrends = new ArrayList<>();
        for (int i = 0; i < resMonReportTeleplayNetRptTrends.size(); i++) {
            ResMonReportTeleplayNetRptTrend mon = resMonReportTeleplayNetRptTrends.get(i);
            Integer teleplayId = Integer.valueOf(mon.getTeleplay());
            ResReportTeleplayNetRptTrend resTrend = BeanUtil.copyProperties(mon,ResReportTeleplayNetRptTrend.class);

            //-------------------- 当前电视剧标题 --------------------
            ResReportTeleplayNetRptTrend.TeleplayFK teleplayFK=new ResReportTeleplayNetRptTrend.TeleplayFK();
            for (Teleplay t : teleplays) {
                int tmpPerson = t.getId();
                if (teleplayId == tmpPerson) {
                    teleplayFK.setId(teleplayId);
                    teleplayFK.setTitle(t.getTitle());
                }
            }
            //-------------------- 当前电视剧标题 --------------------
            resTrend.setTeleplay_fk(teleplayFK);

            resTrends.add(resTrend);
        }

        return rank(totalCount,totalPage,current,gteDate,lteDate,resTrends);
    }


    public R<IPage<ResRank>> reportZyRptTrend(ReqReportZyRptTrend req) {
        int period = req.getPeriod();
        int current = req.getCurrent();
        int size = req.getSize();
        LocalDate gteDate=getGteDate(period);
        LocalDate lteDate=getYesterdayLtDate();

        // 查询条件
        Criteria criteria = Criteria.where("period").is(period);
        Query query = Query.query(criteria);
        // 查询记录总数 数据总页数 放在分页条件之前
        int totalCount = (int) mongoTemplate.count(query, ResMonReportZyRptTrend.class);
        int totalPage = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;

        // 设置起始数
        query.skip((current - 1) * size).limit(size);
        query.with(Sort.by(Sort.Direction.ASC, "report_1905_rank"));
        List<ResMonReportZyRptTrend> resMGs=mongoTemplate.find(query, ResMonReportZyRptTrend.class);
        // 提取teleplay id去数据库查询电视剧信息
        Integer[] zyIds = resMGs.stream().map(e -> e.getZy()).collect(Collectors.toList())
                .toArray(new Integer[resMGs.size()]);
        List<Zy> zys=zyIds.length>0?metaService.getZys(zyIds):new ArrayList<>();

        // 组装数据
        List<ResReportZyRptTrend> resTrends = new ArrayList<>();
        for (int i = 0; i < resMGs.size(); i++) {
            ResMonReportZyRptTrend mon = resMGs.get(i);
            Integer zyId = mon.getZy();
            ResReportZyRptTrend resTrend = BeanUtil.copyProperties(mon,ResReportZyRptTrend.class);

            //-------------------- 当前电视剧标题 --------------------
            ResReportZyRptTrend.ZyFK fk=new ResReportZyRptTrend.ZyFK();
            for (Zy t : zys) {
                int tmpPerson = t.getId();
                if (zyId == tmpPerson) {
                    fk.setId(zyId);
                    fk.setTitle(t.getTitle());
                }
            }
            //-------------------- 当前电视剧标题 --------------------
            resTrend.setZy_fk(fk);

            resTrends.add(resTrend);
        }

        return rank(totalCount,totalPage,current,gteDate,lteDate,resTrends);
    }

    public R<IPage<ResRank>> reportZyNetRptTrend(ReqReportZyRptTrend req) {

        int period = req.getPeriod();
        int current = req.getCurrent();
        int size = req.getSize();
        LocalDate gteDate=getGteDate(period);
        LocalDate lteDate=getYesterdayLtDate();

        // 查询条件
        Criteria criteria = Criteria.where("period").is(period);
        Query query = Query.query(criteria);
        // 查询记录总数 数据总页数 放在分页条件之前
        int totalCount = (int) mongoTemplate.count(query, ResMonReportZyNetRptTrend.class);
        int totalPage = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;

        // 设置起始数
        query.skip((current - 1) * size).limit(size);
        query.with(Sort.by(Sort.Direction.ASC, "report_1905_rank"));
        List<ResMonReportZyNetRptTrend> resMGs=mongoTemplate.find(query, ResMonReportZyNetRptTrend.class);
        // 提取teleplay id去数据库查询电视剧信息
        Integer[] zyIds = resMGs.stream().map(e -> e.getZy()).collect(Collectors.toList())
                .toArray(new Integer[resMGs.size()]);
        List<Zy> zys=zyIds.length>0?metaService.getZys(zyIds):new ArrayList<>();

        // 组装数据
        List<ResReportZyRptTrend> resTrends = new ArrayList<>();
        for (int i = 0; i < resMGs.size(); i++) {
            ResMonReportZyNetRptTrend mon = resMGs.get(i);
            Integer zyId = mon.getZy();
            ResReportZyRptTrend resTrend = BeanUtil.copyProperties(mon,ResReportZyRptTrend.class);

            //-------------------- 当前电视剧标题 --------------------
            ResReportZyRptTrend.ZyFK fk=new ResReportZyRptTrend.ZyFK();
            for (Zy t : zys) {
                int tmpPerson = t.getId();
                if (zyId == tmpPerson) {
                    fk.setId(zyId);
                    fk.setTitle(t.getTitle());
                }
            }
            //-------------------- 当前电视剧标题 --------------------
            resTrend.setZy_fk(fk);

            resTrends.add(resTrend);
        }

        return rank(totalCount,totalPage,current,gteDate,lteDate,resTrends);
    }



    private R rank(int totalCount,int totalPage,int current,LocalDate gteDate,LocalDate ltDate,Object results){
        ResRank resRank=new ResRank();
        resRank.setCount(totalCount);
        resRank.setPages(totalPage);
        resRank.setCurrent(current);
        resRank.setData_time__gte(gteDate);
        resRank.setData_time__lte(ltDate);
        resRank.setSystime(LocalDateTime.now());
        resRank.setToday_reamin_second(DateUtil.getDayRemainingTime(new Date()));
        resRank.setResults(results);
        return R.OK(resRank);
    }

    private LocalDate getGteDate(int period){
        LocalDate ltDate=getYesterdayLtDate();//获取lt日期
        LocalDate gteDate=period<=1?ltDate:ltDate.minusDays(period-1); // >=
        return gteDate;
    }

    private LocalDate getYesterdayLtDate(){
        LocalDate ltDate=LocalDate.now().minusDays(1); // <
        return ltDate;
    }
}
