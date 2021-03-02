package com.vlinkage.xunyee.api.vdata.service;


import cn.hutool.core.bean.BeanUtil;
import com.vlinkage.ant.meta.entity.Person;
import com.vlinkage.ant.meta.entity.Teleplay;
import com.vlinkage.ant.meta.entity.Zy;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.meta.service.MetaService;
import com.vlinkage.xunyee.entity.request.ReqReportPersonRptTrend;
import com.vlinkage.xunyee.entity.request.ReqReportTeleplayRptTrend;
import com.vlinkage.xunyee.entity.request.ReqReportZyRptTrend;
import com.vlinkage.xunyee.entity.response.*;
import com.vlinkage.xunyee.utils.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VdataService {

    @Value("${sys-config.image-path}")
    private String imagePath;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MetaService metaService;

    public R reportPersonRptTrend(ReqReportPersonRptTrend req) {

        int period = req.getPeriod();
        int current = req.getCurrent();
        int size = req.getSize();

        // 查询条件
        Criteria criteria = Criteria.where("period").is(period);
        Query query = Query.query(criteria);
        // 查询记录总数 数据总页数 放在分页条件之前
        int totalCount = (int) mongoTemplate.count(query, ResMonPersonCheckCount.class);
        int totalPage = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;

        // 设置起始数
        query.skip((current - 1) * size).limit(size);
        query.with(Sort.by(Sort.Direction.ASC, "report_1912_teleplay_rank"));
        List<ResMonReportPersonRptTrend> resMonReportPersonRptTrends=mongoTemplate.find(query,ResMonReportPersonRptTrend.class);
        // 提取person id去数据库查询艺人信息
        Integer[] personIds = resMonReportPersonRptTrends.stream().map(e -> e.getPerson()).collect(Collectors.toList())
                .toArray(new Integer[resMonReportPersonRptTrends.size()]);
        List<Person> persons=personIds.length>0?metaService.getPerson(personIds):new ArrayList<>();


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
                    personFK.setAvatar_custom(imagePath+p.getAvatar_custom());
                    personFK.setZh_name(p.getZh_name());
                }
            }
            //-------------------- 当前艺人头像昵称 --------------------
            resReportPersonRptTrend.setPerson_fk(personFK);

            resTrends.add(resReportPersonRptTrend);
        }

        return rank(totalCount,totalPage,current,period,resTrends);

    }

    public R reportPersonRptTrendZy(ReqReportPersonRptTrend req) {
        int period = req.getPeriod();
        int current = req.getCurrent();
        int size = req.getSize();

        // 查询条件
        Criteria criteria = Criteria.where("period").is(period);
        Query query = Query.query(criteria);
        // 查询记录总数 数据总页数 放在分页条件之前
        int totalCount = (int) mongoTemplate.count(query, ResMonReportPersonZyRptTrend.class);
        int totalPage = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;

        // 设置起始数
        query.skip((current - 1) * size).limit(size);
        query.with(Sort.by(Sort.Direction.ASC, "report_1912_zy_rank"));
        List<ResMonReportPersonZyRptTrend> resMongos=mongoTemplate.find(query,ResMonReportPersonZyRptTrend.class);
        // 提取person id去数据库查询艺人信息
        Integer[] personIds = resMongos.stream().map(e -> e.getPerson()).collect(Collectors.toList())
                .toArray(new Integer[resMongos.size()]);
        List<Person> persons=personIds.length>0?metaService.getPerson(personIds):new ArrayList<>();


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
                    personFK.setAvatar_custom(imagePath+p.getAvatar_custom());
                    personFK.setZh_name(p.getZh_name());
                }
            }
            //-------------------- 当前艺人头像昵称 --------------------
            resTrend.setPerson_fk(personFK);

            resTrends.add(resTrend);
        }

        return rank(totalCount,totalPage,current,period,resTrends);
    }

    public R reportTeleplayRptTrend(ReqReportTeleplayRptTrend req) {
        int period = req.getPeriod();
        int current = req.getCurrent();
        int size = req.getSize();

        // 查询条件
        Criteria criteria = Criteria.where("period").is(period);
        if (StringUtils.isNotEmpty(req.getTeleplay__title__icontains())){
            criteria.regex(".*?\\" +req.getTeleplay__title__icontains()+ ".*");
        }
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

        return rank(totalCount,totalPage,current,period,resTrends);
    }

    public R reportTeleplayRptTrendNet(ReqReportTeleplayRptTrend req) {
        int period = req.getPeriod();
        int current = req.getCurrent();
        int size = req.getSize();

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

        return rank(totalCount,totalPage,current,period,resTrends);
    }


    public R reportZyRptTrend(ReqReportZyRptTrend req) {
        int period = req.getPeriod();
        int current = req.getCurrent();
        int size = req.getSize();

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

        return rank(totalCount,totalPage,current,period,resTrends);
    }

    public R reportZyNetRptTrend(ReqReportZyRptTrend req) {

        int period = req.getPeriod();
        int current = req.getCurrent();
        int size = req.getSize();

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

        return rank(totalCount,totalPage,current,period,resTrends);
    }



    private R rank(int totalCount,int totalPage,int current,int period,Object results){
        LocalDate nowDate=LocalDate.now();//今天
        LocalDate gteDate; // >=
        LocalDate ltDate; // <
        if(period<=1){//获取今天签到榜
            gteDate=nowDate;
            ltDate=gteDate.plusDays(1); // <
        }else{
            gteDate=nowDate.minusDays(period);// 减去 7||30
            ltDate=nowDate;
        }
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
}
