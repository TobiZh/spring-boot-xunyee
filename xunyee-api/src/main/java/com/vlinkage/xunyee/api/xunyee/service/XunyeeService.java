package com.vlinkage.xunyee.api.xunyee.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vlinkage.ant.meta.entity.Person;
import com.vlinkage.ant.xunyee.entity.*;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.meta.service.MetaService;
import com.vlinkage.xunyee.entity.ReqMyPage;
import com.vlinkage.xunyee.entity.request.ReqFeedback;
import com.vlinkage.xunyee.entity.request.ReqPersonCheckCount;
import com.vlinkage.xunyee.entity.request.ReqPic;
import com.vlinkage.xunyee.entity.response.*;
import com.vlinkage.xunyee.utils.CopyListUtil;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class XunyeeService {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MetaService metaService;


    public R<List<ResPic>> getPic(ReqPic req) {
        LocalDateTime nowDate=LocalDateTime.now();

        QueryWrapper qw=new QueryWrapper();
        qw.eq("type_id",req.getType());
        if (req.getIs_enabled_app()!=null){
            qw.eq("is_enabled_5",req.getIs_enabled_mini()==0?false:true);
        }
        if (req.getIs_enabled_app()!=null){
            qw.eq("is_enabled_6",req.getIs_enabled_app()==0?false:true);
        }

        qw.orderByAsc("sequence");
        qw.le("start_time",nowDate);// >=
        qw.ge("finish_time",nowDate);// <=
        List<XunyeePic> xunyeePics=new XunyeePic().selectList(qw);
        List<ResPic> resPics=CopyListUtil.copyListProperties(xunyeePics, ResPic.class);
        return R.OK(resPics);
    }

    public R<List<ResNavigation>> getNavigation() {
        QueryWrapper qw=new QueryWrapper();
        qw.orderByAsc("orderby");
        qw.eq("is_deleted",0);
        qw.eq("is_enabled",1);
        List<XunyeeNavigation> navigation=new XunyeeNavigation().selectList(qw);
        List<ResNavigation> resNavigations=CopyListUtil.copyListProperties(navigation,ResNavigation.class);
        return R.OK(resNavigations);
    }

    public R<List<ResSearchHot>> getSearchHot() {
        QueryWrapper qw=new QueryWrapper();
        qw.eq("is_deleted",false);
        qw.orderByAsc("orderby");
        qw.select("id","name");
        List<XunyeeSearchHot> searchHot=new XunyeeSearchHot().selectList(qw);
        List<ResSearchHot> resSearchHots=CopyListUtil.copyListProperties(searchHot,ResSearchHot.class);
        return R.OK(resSearchHots);
    }

    public R feedback(int userId, ReqFeedback req) {
        XunyeeFeedback feedback= BeanUtil.copyProperties(req,XunyeeFeedback.class);
        feedback.setVcuser_id(userId);
        if (feedback.insert()){
            return R.OK();
        }
        return R.ERROR();
    }

    public R<IPage<ResSystemNotification>> systemNotification(int userId, ReqMyPage myPage) {

        QueryWrapper qw=new QueryWrapper();
        qw.eq("receive_vcuser_id",userId);
        qw.eq("receive_vcuser_id",0);//所有人都能收到的
        Page page=new Page(myPage.getCurrent(),myPage.getSize());
        IPage<ResSystemNotification> iPage=new XunyeeSystemNotification().selectPage(page,qw);
        iPage.setRecords(CopyListUtil.copyListProperties(iPage.getRecords(), ResSystemNotification.class));

        return R.OK(iPage);
    }

    public R systemNotificationRead(int id) {
        XunyeeSystemNotification notification=new XunyeeSystemNotification().selectById(id);
        if (notification!=null){
            if (notification.getIs_read()){
                return R.ERROR("已标记过");
            }
            notification.setIs_read(true);
            notification.setRead_time(new Date());
            if (notification.updateById()){
                return R.OK();
            }
            return R.ERROR();
        }
        return R.ERROR("该通知不存在");
    }

    public R vcuserBenefit(int userId) {
        QueryWrapper qw=new QueryWrapper();
        qw.select("start_time","finish_time");
        qw.eq("vcuser_id",userId);
        XunyeeVcuserBenefit benefit=new XunyeeVcuserBenefit().selectOne(qw);
        ResBenefit resBenefit=BeanUtil.copyProperties(benefit,ResBenefit.class);
        return R.OK(resBenefit);
    }

    public R personCheckCount(ReqPersonCheckCount req) {

        Integer userId=23;//用户id

        int period=req.getPeriod();
        int current = req.getCurrent();
        int size = req.getSize();
        int rankStart=(current-1)*size+1; // 分页rank起始值
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate nowDate=LocalDate.parse("2019-12-17",df);

//        LocalDate nowDate=LocalDate.now();//当前时间
        LocalDate gteDate; // >=
        LocalDate ltDate; // <
        if(period<=1){//获取今天签到榜
            gteDate=nowDate;
            ltDate=nowDate.plusDays(1);
        }else{
            ltDate=nowDate.plusDays(1);
            gteDate=nowDate.minusDays(period);// 减去 7||30
        }

        // 查询条件
        Criteria criteria = Criteria.where("data_time").gte(gteDate).lt(ltDate);
        Query query = Query.query(criteria);
        // 查询记录总数
        int totalCount = (int) mongoTemplate.count(query, ResMonPersonCheckCount.class);
        // 数据总页数
        int totalPage = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;

        // 根据person分组 check 求和
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("person").sum("check").as("check"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "check")),
                Aggregation.skip((current - 1) * size),
                Aggregation.limit(size)
        );
        AggregationResults<ResMonPersonCheckCount> outputTypeCount = mongoTemplate.aggregate(aggregation, "person__check__count",
                ResMonPersonCheckCount.class);
        List<ResMonPersonCheckCount> resMonPersonCheckCounts = outputTypeCount.getMappedResults();


        // 查询当前用户关注的艺人和当天签到数
        List<ResMonUserPersonCheck> checks=null;
        if(period<=1&&userId!=null){
            checks=mongoTemplate.find(new Query(Criteria.where("vcuser").is(userId).and("updated").gte(gteDate).lt(ltDate)),
                    ResMonUserPersonCheck.class);

        }

        // 数据库中查询开启签到的艺人
        List<Person> personList=metaService.getPersonCheck();

        // 组装数据
        List<ResPersonCheckCount> resPersonCheckCounts=new ArrayList<>();
        for (int i = 0; i < resMonPersonCheckCounts.size(); i++) {
            ResMonPersonCheckCount mon=resMonPersonCheckCounts.get(i);
            Integer personId=Integer.valueOf(mon.getId());

            ResPersonCheckCount resPersonCheckCount=new ResPersonCheckCount();
            resPersonCheckCount.setCheck(mon.getCheck());
            resPersonCheckCount.setRank(rankStart+i);
            resPersonCheckCount.setPerson(personId);
            resPersonCheckCount.setId(personId);

            //-------------------- 当前用户>>艺人签到数 --------------------
            if (period<=1){
                for (int j = 0; j < checks.size(); j++) {
                    int tmpPerson=checks.get(j).getPerson();
                    if (personId==tmpPerson){
                        resPersonCheckCount.setCheck_my(checks.get(j).getCheck());
                    }
                }
            }else{
                resPersonCheckCount.setCheck_my(0);
            }
            //-------------------- 当前用户>>艺人签到数 --------------------

            //-------------------- 当前艺人头像昵称 --------------------
            for (Person p:personList){
                int tmpPerson=p.getId();
                if (personId==tmpPerson){
                    resPersonCheckCount.setAvatar_custom(p.getAvatar_custom());
                    resPersonCheckCount.setZh_name(p.getZh_name());
                }
            }
            //-------------------- 当前艺人头像昵称 --------------------

            resPersonCheckCounts.add(resPersonCheckCount);
        }

        IPage iPage=new Page();
        iPage.setCurrent(current);
        iPage.setTotal(totalCount);
        iPage.setPages(totalPage);
        iPage.setSize(size);
        iPage.setRecords(resPersonCheckCounts);

        return R.OK(iPage);
    }
}
