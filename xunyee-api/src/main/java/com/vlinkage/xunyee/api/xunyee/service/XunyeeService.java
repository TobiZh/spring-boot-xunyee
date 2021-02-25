package com.vlinkage.xunyee.api.xunyee.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vlinkage.ant.xunyee.entity.*;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.entity.ReqMyPage;
import com.vlinkage.xunyee.entity.request.ReqFeedback;
import com.vlinkage.xunyee.entity.request.ReqPersonCheckCount;
import com.vlinkage.xunyee.entity.request.ReqPic;
import com.vlinkage.xunyee.entity.response.*;
import com.vlinkage.xunyee.utils.CopyListUtil;
import io.swagger.annotations.ApiModelProperty;
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
import java.util.Date;
import java.util.List;

@Service
public class XunyeeService {

    @Autowired
    private MongoTemplate mongoTemplate;

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

        int period=req.getPeriod();
        int current = req.getCurrent();
        int size = req.getSize();

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate nowDate=LocalDate.parse("2019-11-23",df);

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


        Criteria criteria = Criteria.where("data_time").gte(gteDate).lt(ltDate);
        Query query = Query.query(criteria);
        query.fields().exclude("id");
        query.fields().exclude("data_time");
        // 设置起始数
        query.skip((current - 1) * size)
                // 设置查询条数
                .limit(size);

        // 查询记录总数
        int totalCount = (int) mongoTemplate.count(query, ResMonPersonCheckCount.class);
        // 数据总页数
        int totalPage = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;

        // 按签到数排序
        query.with(Sort.by(Sort.Direction.DESC, "check"));

        // 根据person分组 check 求和
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(criteria),
                Aggregation.group("person").first("person").as("person").sum("check").as("check"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "check")));


        AggregationResults<ResMonPersonCheckCount> outputTypeCount = mongoTemplate.aggregate(aggregation, "person__check_count",
                ResMonPersonCheckCount.class);
        List<ResMonPersonCheckCount> list = outputTypeCount.getMappedResults();


//        List<ResMonPersonCheckCount> list=mongoTemplate.find(query,ResMonPersonCheckCount.class);


        IPage iPage=new Page();
        iPage.setCurrent(current);
        iPage.setTotal(totalCount);
        iPage.setPages(totalPage);
        iPage.setSize(size);
        iPage.setRecords(list);

        return R.OK(iPage);
    }
}
