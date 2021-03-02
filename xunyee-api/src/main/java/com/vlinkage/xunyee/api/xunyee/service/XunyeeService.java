package com.vlinkage.xunyee.api.xunyee.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mongodb.client.result.UpdateResult;
import com.vlinkage.ant.meta.entity.Person;
import com.vlinkage.ant.meta.entity.Zy;
import com.vlinkage.ant.xunyee.entity.*;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.meta.service.MetaService;
import com.vlinkage.xunyee.entity.ReqMyPage;
import com.vlinkage.xunyee.entity.request.*;
import com.vlinkage.xunyee.entity.response.*;
import com.vlinkage.xunyee.utils.CopyListUtil;
import com.vlinkage.xunyee.utils.DateUtil;
import com.vlinkage.xunyee.utils.MongodbUtils;
import com.vlinkage.xunyee.utils.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class XunyeeService {

    @Value("${sys-config.image-path}")
    private String imagePath;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MetaService metaService;


    public R<Map> getPic(ReqPic req) {
        LocalDateTime nowDate=LocalDateTime.now();

        QueryWrapper qw=new QueryWrapper();
        qw.eq("type_id",req.getType());
        if (req.getIs_enabled_5()!=null){
            qw.eq("is_enabled_5",req.getIs_enabled_5()==0?false:true);
        }
        if (req.getIs_enabled_6()!=null){
            qw.eq("is_enabled_6",req.getIs_enabled_6()==0?false:true);
        }
        qw.le("start_time",nowDate);// >=
        qw.ge("finish_time",nowDate);// <=
        qw.orderByAsc("sequence");
        List<XunyeePic> xunyeePics=new XunyeePic().selectList(qw);
        List<ResPic> resPics=CopyListUtil.copyListProperties(xunyeePics, ResPic.class);
        for (ResPic p:resPics){
            p.setPic(imagePath+p.getPic());
        }
        Map map=new HashMap();
        map.put("count",resPics.size());
        map.put("results",resPics);
        return R.OK(map);
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

    public R personCheckCount(Integer userId,ReqPersonCheckCount req) {

        int period=req.getPeriod();
        int current = req.getCurrent();
        int size = req.getSize();
        int rankStart=(current-1)*size+1; // 分页rank起始值

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

        // 查询条件
        Criteria criteria = Criteria.where("data_time").gte(gteDate).lt(ltDate);
        // 根据person分组 check 求和
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("person").sum("check").as("check"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC,"check"))
        );
        AggregationResults<ResMonPersonCheckCount> outputTypeCount = mongoTemplate.aggregate(aggregation, "person__check__count",
                ResMonPersonCheckCount.class);
        // 查询mongo中有签到数据的艺人
        List<ResMonPersonCheckCount> resMgs = outputTypeCount.getMappedResults();
        if (resMgs.size()<=0){
            //todo 这里做一个读取缓存
        }


        // 查询所有的可签到艺人 大概500个
        List<Person> persons=metaService.getPersonByXunyeeCheck();
        // 查询记录总数 数据总页数
        int totalCount=persons.size();
        int totalPage = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;
        // 查询当前用户关注的艺人和当天签到数
        List<ResMonUserPersonCheck> userPersonChecks=period<=1&&userId!=null?mongoTemplate.find(new Query(Criteria.where("vcuser").is(userId).and("updated").gte(gteDate).lt(ltDate)),
                ResMonUserPersonCheck.class):new ArrayList<>();

        // 组装数据
        List<ResPersonCheckCount> resCheckCounts=new ArrayList<>();
        for (Person p:persons){
            int personId=p.getId();
            ResPersonCheckCount resPersonCheckCount=new ResPersonCheckCount();
            resPersonCheckCount.setPerson(personId);
            resPersonCheckCount.setId(personId);
            resPersonCheckCount.setVcuser_person("");
            resPersonCheckCount.setAvatar_custom(p.getAvatar_custom());
            resPersonCheckCount.setZh_name(p.getZh_name());
            for (int i = 0; i < resMgs.size(); i++) {
                ResMonPersonCheckCount mon=resMgs.get(i);
                Integer tmpPerson=mon.getId();
                //-------------------- 当前用户>>艺人签到数 --------------------
                if (period<=1){
                    for (int j = 0; j < userPersonChecks.size(); j++) {
                        if (personId==userPersonChecks.get(j).getPerson()){
                            resPersonCheckCount.setCheck_my(userPersonChecks.get(j).getCheck());
                            break;
                        }
                    }
                }
                //-------------------- 当前用户>>艺人签到数 --------------------

                //-------------------- 当前艺人头签到数 --------------------
                if (personId==tmpPerson){
                    resPersonCheckCount.setCheck(mon.getCheck());
                    break;
                }
                //-------------------- 当前艺人头签到数 --------------------
            }
            resCheckCounts.add(resPersonCheckCount);
        }
        Collections.sort(resCheckCounts, Comparator.comparing(ResPersonCheckCount::getCheck).reversed());
        for (int i = 0; i < resCheckCounts.size(); i++) {
            resCheckCounts.get(i).setRank(rankStart+i);
        }
        if(totalCount>=current*size){
            resCheckCounts=PageUtil.startPage(resCheckCounts,current,size);
        }else{
            resCheckCounts=new ArrayList<>();
        }


        ResRank resRank=new ResRank();
        resRank.setCount(totalCount);
        resRank.setPages(totalPage);
        resRank.setCurrent(current);
        resRank.setData_time__gte(gteDate);
        resRank.setData_time__lte(nowDate.minusDays(1));
        resRank.setSystime(LocalDateTime.now());
        resRank.setToday_reamin_second(DateUtil.getDayRemainingTime(new Date()));
        resRank.setResults(resCheckCounts);
        return R.OK(resRank);
    }

    public R personCheckCountIdol(Integer userId,ReqMyPage req) {

        int current = req.getCurrent();
        int size = req.getSize();

        LocalDate nowDate=LocalDate.now();//今天
        LocalDate gteDate=nowDate; // >=
        LocalDate ltDate=gteDate.plusDays(1); // <; // <


        Criteria criteria=new Criteria().where("vcuser").is(userId).and("is_enabled").is(true);
        Query query=new Query(criteria);
        // 查询记录总数 数据总页数 放在分页条件之前
        int totalCount = (int) mongoTemplate.count(query, ResMonUserPerson.class);
        int totalPage = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;
        // 分页 排序
        query.skip((current - 1) * size).limit(size).with(Sort.by(Sort.Direction.DESC, "updated"));
        // 查询我关注的艺人
        List<ResMonUserPerson> resMGs=mongoTemplate.find(query, ResMonUserPerson.class);
        // 提取teleplay id去数据库查询电视剧信息
        Integer[] personIds = resMGs.stream().map(e -> e.getPerson()).collect(Collectors.toList())
                .toArray(new Integer[resMGs.size()]);
        // 查询数据库艺人信息
        List<Person> persons=personIds.length>0?metaService.getPerson(personIds):new ArrayList<>();

        // 查询当前用户关注的艺人和当天签到数
        List<ResMonUserPersonCheck> resMonUserPersonChecks=mongoTemplate.find(new Query(Criteria.where("vcuser").is(userId).and("updated").gte(gteDate).lt(ltDate)), ResMonUserPersonCheck.class);

        // 组装数据
        List<ResPersonCheckCount> resCheckCounts=new ArrayList<>();
        for (ResMonUserPerson pc:resMGs){
            int personId=pc.getPerson();
            ResPersonCheckCount resPersonCheckCount=new ResPersonCheckCount();
            for (Person p:persons){
                int tmpPerson=p.getId();
                if (personId==tmpPerson){
                    resPersonCheckCount.setPerson(personId);
                    resPersonCheckCount.setId(personId);
                    resPersonCheckCount.setVcuser_person("");
                    resPersonCheckCount.setAvatar_custom(p.getAvatar_custom());
                    resPersonCheckCount.setZh_name(p.getZh_name());
                    break;
                }
            }
            for (ResMonUserPersonCheck c:resMonUserPersonChecks) {
                int tmpPerson=c.getPerson();
                if (personId==tmpPerson){
                    resPersonCheckCount.setCheck_my(c.getCheck());
                    break;
                }
            }

            resCheckCounts.add(resPersonCheckCount);
        }

        ResRank resRank=new ResRank();
        resRank.setCount(totalCount);
        resRank.setPages(totalPage);
        resRank.setCurrent(current);
        resRank.setData_time__gte(gteDate);
        resRank.setData_time__lte(ltDate.minusDays(1));
        resRank.setSystime(LocalDateTime.now());
        resRank.setToday_reamin_second(DateUtil.getDayRemainingTime(new Date()));
        resRank.setResults(resCheckCounts);
        return R.OK(resRank);
    }

    public R vcuserPerson(int userId, ReqUserPersonCheck req) {
        Query query=Query.query(Criteria.where("vcuser").is(userId).and("person").is(req.getPerson()));
        Update update=Update.update("is_enabled",req.getIs_enabled()==0?false:true).set("updated",LocalDateTime.now());
        UpdateResult result=mongoTemplate.updateFirst(query,update,"vc_user__person");
        if (result.getModifiedCount()>0){
            return R.OK();
        }
       return R.ERROR();
    }

    public R<ResXunyeeBenefitPrice> benefitPrice() {
        LocalDateTime nowDate=LocalDateTime.now();
        QueryWrapper qw=new QueryWrapper();
        qw.eq("is_enabled",true);
        qw.le("start_time",nowDate);// >=
        qw.ge("finish_time",nowDate);// <=
        qw.orderByAsc("quantity");
        List<XunyeeBenefitPrice> benefitPrices=new XunyeeBenefitPrice().selectList(qw);
        List<ResXunyeeBenefitPrice> resXunyeeBenefitPrices=CopyListUtil.copyListProperties(benefitPrices,ResXunyeeBenefitPrice.class);
        return R.OK(resXunyeeBenefitPrices);
    }


    /**
     * 会员一天 3次只能签一个人
     * 非会员一天 3人1次
     * @param userId
     * @param req
     * @return
     */
    public R vcuserPersonCHeck(int userId, ReqPersonCheck req) {
        int personId=req.getPerson();
        Person person=metaService.getPersonById(personId);
        if (!person.getIs_xunyee_check()){
            return R.ERROR("该艺人已关闭签到");
        }
        LocalDate gteDate=LocalDate.now(); // >=
        LocalDate ltDate=gteDate.plusDays(1); // <; // <
        QueryWrapper qw=new QueryWrapper();
        qw.eq("vcuser_id",userId);
        qw.ge("start_time",gteDate);//<=
        qw.lt("finish_time",ltDate);// >
        XunyeeVcuserBenefit vcuserBenefit=new XunyeeVcuserBenefit().selectOne(qw);

        // 查询当前用户关注的艺人和当天签到数
        List<ResMonUserPersonCheck> resPersonChecks=mongoTemplate.find(new Query(Criteria.where("vcuser")
                .is(userId).and("updated")
                .gte(gteDate).lt(ltDate)),
                ResMonUserPersonCheck.class);
        int checkCount = resPersonChecks.stream().collect(Collectors.summingInt(ResMonUserPersonCheck::getCheck));
        if (checkCount>=3){
            return R.ERROR("每天对所有艺人的签到数不能超过3。");
        }
        boolean b = resPersonChecks.stream().anyMatch(task -> task.getPerson().equals(personId));
        if (b){
            return R.ERROR("今天已经签到过了，明天再来吧");
        }
        ReqMonUserPersonCheck reqCheck=new ReqMonUserPersonCheck();
        reqCheck.setVcuser(userId);
        reqCheck.setPerson(personId);
        reqCheck.setUpdated(LocalDateTime.now());
        reqCheck.setCheck(vcuserBenefit==null?1:3);
        ReqMonUserPersonCheck check=mongoTemplate.insert(reqCheck);
        if (check==null){
            return R.ERROR("签到失败，请稍后再试");
        }
        return R.OK();
    }
}
