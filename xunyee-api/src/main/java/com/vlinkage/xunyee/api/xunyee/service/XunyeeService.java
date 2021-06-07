package com.vlinkage.xunyee.api.xunyee.service;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.binarywang.wxpay.bean.order.WxPayAppOrderResult;
import com.mongodb.client.result.UpdateResult;
import com.vlinkage.ant.meta.entity.Person;
import com.vlinkage.ant.star.entity.SdbJdSale;
import com.vlinkage.ant.star.entity.SdbPersonGallery;
import com.vlinkage.ant.xunyee.entity.*;
import com.vlinkage.ant.xunyee.mapper.XunyeeVcuserBenefitMapper;
import com.vlinkage.xunyee.entity.result.R;
import com.vlinkage.xunyee.entity.result.code.ResultCode;
import com.vlinkage.xunyee.api.meta.MetaService;
import com.vlinkage.xunyee.api.pay.service.PayService;
import com.vlinkage.xunyee.api.star.StarService;
import com.vlinkage.xunyee.entity.ReqMyPage;
import com.vlinkage.xunyee.entity.request.*;
import com.vlinkage.xunyee.entity.response.*;
import com.vlinkage.xunyee.mapper.MyMapper;
import com.vlinkage.xunyee.utils.CopyListUtil;
import com.vlinkage.xunyee.utils.DateUtil;
import com.vlinkage.xunyee.utils.ImageHostUtil;
import com.vlinkage.xunyee.utils.OrderCodeFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.impl.NoOpLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class XunyeeService {

    @Autowired
    private ImageHostUtil imageHostUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MetaService metaService;
    @Autowired
    private StarService starService;
    @Autowired
    private PayService payService;
    @Autowired
    private MyMapper myMapper;

    @Resource
    private XunyeeVcuserBenefitMapper xunyeeVcuserBenefitMapper;


    public R<ResPic> getAdLaunch(ReqPic req) {
        LocalDateTime nowDate = LocalDateTime.now();

        LambdaQueryWrapper<XunyeePic> qw = new LambdaQueryWrapper();
        qw.eq(XunyeePic::getType_id, 1);
        if (req.getIs_enabled_5() != null) {
            qw.eq(XunyeePic::getIs_enabled_5, req.getIs_enabled_5() == 0 ? false : true);
        }
        if (req.getIs_enabled_6() != null) {
            qw.eq(XunyeePic::getIs_enabled_6, req.getIs_enabled_6() == 0 ? false : true);
        }
        qw.le(XunyeePic::getStart_time, nowDate)
                .ge(XunyeePic::getFinish_time, nowDate)
                .orderByAsc(XunyeePic::getSequence);
        XunyeePic xunyeePic = new XunyeePic().selectOne(qw);
        if (xunyeePic != null) {
            ResPic resPic = BeanUtil.copyProperties(xunyeePic, ResPic.class);
            resPic.setPic(imageHostUtil.absImagePath(resPic.getPic()));
            return R.OK(resPic);
        }
        return R.OK(xunyeePic);
    }


    public R<List<ResPic>> getAdBanner(ReqPic req) {
        LocalDateTime nowDate = LocalDateTime.now();
        LambdaQueryWrapper<XunyeePic> qw = new LambdaQueryWrapper<>();
        qw.eq(XunyeePic::getType_id, 2);//轮播广告
        if (req.getIs_enabled_5() != null) {
            qw.eq(XunyeePic::getIs_enabled_5, req.getIs_enabled_5() == 0 ? false : true);
        }
        if (req.getIs_enabled_6() != null) {
            qw.eq(XunyeePic::getIs_enabled_6, req.getIs_enabled_6() == 0 ? false : true);
        }
        qw.le(XunyeePic::getStart_time, nowDate)// >=
                .ge(XunyeePic::getFinish_time, nowDate)// <=
                .orderByAsc(XunyeePic::getSequence);
        List<XunyeePic> picList = new XunyeePic().selectList(qw);
        List<ResPic> resPics = CopyListUtil.copyListProperties(picList, ResPic.class);
        for (ResPic p : resPics) {
            p.setPic(imageHostUtil.absImagePath(p.getPic()));
        }
        return R.OK(resPics);
    }

    public R<List<ResNavigation>> getNavigation(String source) {

        LambdaQueryWrapper<XunyeeNavigation> qw = new LambdaQueryWrapper<>();
        qw.eq(XunyeeNavigation::getIs_deleted, false);
        switch (source) {
            case "android":
                qw.eq(XunyeeNavigation::getIs_enabled_android, true);
                break;
            case "ios":
                qw.eq(XunyeeNavigation::getIs_enabled_ios, true);
                break;
            case "mini":
                qw.eq(XunyeeNavigation::getIs_enabled_mini, true);
                break;
        }
        qw.orderByAsc(XunyeeNavigation::getOrderby);
        List<XunyeeNavigation> navigation = new XunyeeNavigation().selectList(qw);
        List<ResNavigation> resNavigations = CopyListUtil.copyListProperties(navigation, ResNavigation.class);
        for (ResNavigation resNavigation : resNavigations) {
            resNavigation.setIcon(imageHostUtil.absImagePath(resNavigation.getIcon()));
            if (resNavigation.getType() == 2) {// type=2 应用内部页面
                String params = resNavigation.getParams();
                Map<String, Object> map = JSONObject.parseObject(params, Map.class);
                resNavigation.setParams(map.get(source).toString());
            }
        }
        return R.OK(resNavigations);
    }

    public R<List<ResSearchHot>> getSearchHot() {
        LambdaQueryWrapper<XunyeeSearchHot> qw = new LambdaQueryWrapper<>();
        qw.select(XunyeeSearchHot::getId, XunyeeSearchHot::getName)
                .eq(XunyeeSearchHot::getIs_deleted, false)
                .orderByAsc(XunyeeSearchHot::getOrderby);

        List<XunyeeSearchHot> searchHot = new XunyeeSearchHot().selectList(qw);
        List<ResSearchHot> resSearchHots = CopyListUtil.copyListProperties(searchHot, ResSearchHot.class);
        return R.OK(resSearchHots);
    }

    public R feedback(int userId, ReqFeedback req) {
        XunyeeFeedback feedback = BeanUtil.copyProperties(req, XunyeeFeedback.class);
        feedback.setVcuser_id(userId);
        if (feedback.insert()) {
            return R.OK();
        }
        return R.ERROR();
    }

    public R<IPage<ResSystemNotification>> systemNotification(int userId, ReqMyPage myPage) {

        LambdaQueryWrapper<XunyeeSystemNotification> qw = new LambdaQueryWrapper<>();
        qw.eq(XunyeeSystemNotification::getReceive_vcuser_id, userId)
                .or()
                .eq(XunyeeSystemNotification::getReceive_vcuser_id, 0)
                .orderByDesc(XunyeeSystemNotification::getCreated);
        Page page = new Page(myPage.getCurrent(), myPage.getSize());
        IPage<ResSystemNotification> iPage = new XunyeeSystemNotification().selectPage(page, qw);
        iPage.setRecords(CopyListUtil.copyListProperties(iPage.getRecords(), ResSystemNotification.class));

        return R.OK(iPage);
    }


    public R<Integer> systemNotificationCount(int userId) {

        LambdaQueryWrapper<XunyeeSystemNotification> qw = new LambdaQueryWrapper<>();
        qw.eq(XunyeeSystemNotification::getIs_read, 0)
                .eq(XunyeeSystemNotification::getReceive_vcuser_id, userId);
        int count = new XunyeeSystemNotification().selectCount(qw);
        return R.OK(count);
    }

    public R systemNotificationRead(int userId, int id) {
        XunyeeSystemNotification notification = new XunyeeSystemNotification().selectById(id);
        if (notification.getReceive_vcuser_id() != userId) {
            return R.ERROR("不是你的通知");
        }
        if (notification != null) {
            if (notification.getIs_read() == 1) {
                return R.ERROR("已标记过");
            }
            notification.setIs_read(1);
            notification.setRead_time(new Date());
            if (notification.updateById()) {
                return R.OK();
            }
            return R.ERROR();
        }
        return R.ERROR("该通知不存在");
    }

    public R systemNotificationReadALl(int userId) {
        String sql = "update xunyee_system_notification set is_read=1,read_time=CURRENT_TIMESTAMP " +
                "where receive_vcuser_id=" + userId + " and is_read=0";
        int count = jdbcTemplate.update(sql);
        if (count < 0) {
            return R.ERROR();
        }
        return R.OK();
    }


    public R vcuserBenefit(int userId) {
        LambdaQueryWrapper<XunyeeVcuserBenefit> qw = new LambdaQueryWrapper<>();
        qw.select(XunyeeVcuserBenefit::getStart_time, XunyeeVcuserBenefit::getFinish_time)
                .eq(XunyeeVcuserBenefit::getVcuser_id, userId)
                .le(XunyeeVcuserBenefit::getStart_time,LocalDateTime.now())
                .ge(XunyeeVcuserBenefit::getFinish_time,LocalDateTime.now());
        XunyeeVcuserBenefit benefit = new XunyeeVcuserBenefit().selectOne(qw);
        if (benefit == null) {
            return R.ERROR("您还不是会员");
        }
        ResBenefit resBenefit = BeanUtil.copyProperties(benefit, ResBenefit.class);
        return R.OK(resBenefit);
    }

    public R vcuserBenefitCount(int benefit) {
        LambdaQueryWrapper<XunyeeVcuserBenefit> qw = new LambdaQueryWrapper<>();
        qw.eq(XunyeeVcuserBenefit::getBenefit_id, benefit);
        int count = new XunyeeVcuserBenefit().selectCount(qw);
        return R.OK(count);

    }

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

    public R<ResRank<ResPersonCheckCountIdol>> personCheckCountIdol(Integer userId, ReqMyPage req) {

        int current = req.getCurrent();
        int size = req.getSize();

        LocalDate nowDate = LocalDate.now();//今天
        LocalDate gteDate = nowDate; // >=
        LocalDate ltDate = gteDate.plusDays(1); // <; // <


        Criteria criteria = new Criteria().where("vcuser").is(userId).and("is_enabled").is(true);
        Query query = new Query(criteria);
        // 查询记录总数 数据总页数 放在分页条件之前
        int totalCount = (int) mongoTemplate.count(query, ResMonUserPerson.class);
        int totalPage = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;
        // 分页 排序
        query.skip((current - 1) * size).limit(size).with(Sort.by(Sort.Direction.DESC, "updated"));
        // 查询我关注的艺人
        List<ResMonUserPerson> resMGs = mongoTemplate.find(query, ResMonUserPerson.class);
        // 提取teleplay id去数据库查询电视剧信息
        Integer[] personIds = resMGs.stream().map(e -> e.getPerson()).collect(Collectors.toList())
                .toArray(new Integer[resMGs.size()]);
        // 查询数据库艺人信息
        List<Person> persons = personIds.length > 0 ? metaService.getPerson(personIds) : new ArrayList<>();

        // 查询当前用户关注的艺人和当天签到数
        List<ResMonUserPersonCheck> resMonUserPersonChecks = mongoTemplate.find(new Query(Criteria.where("vcuser").is(userId).and("updated").gte(gteDate).lt(ltDate)), ResMonUserPersonCheck.class);


        // 查询当前用户关注的艺人和今年签到的天数
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("vcuser").is(userId).and("updated").gte(DateUtil.getCurrYearFirst(LocalDate.now().getYear()))),
                Aggregation.group("person").count().as("check")
        );
        AggregationResults<ResMonUserPersonCheckDays> res = mongoTemplate.aggregate(aggregation, "vc_user__person__check", ResMonUserPersonCheckDays.class);
        List<ResMonUserPersonCheckDays> resMonData = res.getMappedResults();

        // 组装数据
        List<ResPersonCheckCountIdol> resCheckCounts = new ArrayList<>();
        for (ResMonUserPerson pc : resMGs) {
            int personId = pc.getPerson();
            ResPersonCheckCountIdol resPersonCheckCount = new ResPersonCheckCountIdol();
            for (Person p : persons) {
                int tmpPerson = p.getId();
                if (personId == tmpPerson) {
                    resPersonCheckCount.setPerson(personId);
                    resPersonCheckCount.setId(personId);
                    resPersonCheckCount.setVcuser_person("");
                    resPersonCheckCount.setAvatar_custom(p.getAvatar_custom());
                    resPersonCheckCount.setZh_name(p.getZh_name());
                    break;
                }
            }
            for (ResMonUserPersonCheck c : resMonUserPersonChecks) {
                int tmpPerson = c.getPerson();
                if (personId == tmpPerson) {
                    resPersonCheckCount.setCheck_my(c.getCheck());
                    break;
                }
            }
            for (ResMonUserPersonCheckDays c : resMonData) {
                int tmpPerson = c.getId();
                if (personId == tmpPerson) {
                    resPersonCheckCount.setCheck_days(c.getCheck());
                }
            }
            resCheckCounts.add(resPersonCheckCount);
        }

        ResRank resRank = new ResRank();
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
        Query query = Query.query(Criteria.where("vcuser").is(userId).and("person").is(req.getPerson()));
        Update update = Update.update("is_enabled", req.getIs_enabled() == 0 ? false : true).set("updated", LocalDateTime.now());
        UpdateResult result = mongoTemplate.updateFirst(query, update, "vc_user__person");
        if (result.getModifiedCount() > 0) {
            return R.OK();
        }
        return R.ERROR();
    }

    public R<ResXunyeeBenefitPrice> benefitPrice(Integer benefit) {
        if (benefit==null){
            benefit=1;
        }
        LocalDateTime nowDate = LocalDateTime.now();
        LambdaQueryWrapper<XunyeeBenefitPrice> qw = new LambdaQueryWrapper<>();
        qw.eq(XunyeeBenefitPrice::getIs_enabled, true)
                .eq(XunyeeBenefitPrice::getBenefit_id,benefit)
                .le(XunyeeBenefitPrice::getStart_time, nowDate)// >=
                .ge(XunyeeBenefitPrice::getFinish_time, nowDate)// <=
                .orderByAsc(XunyeeBenefitPrice::getQuantity);
        List<XunyeeBenefitPrice> benefitPrices = new XunyeeBenefitPrice().selectList(qw);
        List<ResXunyeeBenefitPrice> resXunyeeBenefitPrices = CopyListUtil.copyListProperties(benefitPrices, ResXunyeeBenefitPrice.class);
        return R.OK(resXunyeeBenefitPrices);
    }


    /**
     * 会员一天 3次只能签一个人
     * 非会员一天 3人1次
     *
     * @param userId
     * @param personId
     * @return
     */
    public R vcuserPersonCheckVerify(int userId, int personId) {

        Person person = metaService.getPersonById(personId);
        if (person == null || !person.getIs_xunyee_check()) {
            return R.ERROR("该艺人已关闭签到");
        }

        LocalDate gteDate = LocalDate.now(); // >=
        LocalDate ltDate = gteDate.plusDays(1); // <; // <

        LambdaQueryWrapper<XunyeeVcuserBenefit> qw = new LambdaQueryWrapper<>();
        qw.eq(XunyeeVcuserBenefit::getVcuser_id, userId)
                .le(XunyeeVcuserBenefit::getStart_time, gteDate)//<=
                .ge(XunyeeVcuserBenefit::getFinish_time, gteDate);// >
        XunyeeVcuserBenefit userBenefit = new XunyeeVcuserBenefit().selectOne(qw);

        // 查询当前用户关注的艺人和当天签到数
        List<ResMonUserPersonCheck> resPersonChecks = mongoTemplate.find(new Query(Criteria.where("vcuser")
                        .is(userId).and("updated")
                        .gte(gteDate).lt(ltDate)),
                ResMonUserPersonCheck.class);
        // 今日已使用次数
        int checkCounted = resPersonChecks.stream().collect(Collectors.summingInt(ResMonUserPersonCheck::getCheck));
        // 是否已为该艺人签到过 personId需要为int类型
        boolean b = resPersonChecks.stream().anyMatch(task -> task.getPerson() == personId);

        if (b) {
            if (userBenefit != null) {
                return R.ERROR("今天已经签到过了，明天再来吧");
            } else {
                return R.ERROR(ResultCode.USER_NOT_OPEN_VIP);
            }
        }

        if (checkCounted >= 3) {
            return R.ERROR("每天对所有艺人的签到数不能超过3。");
        }

        LocalDateTime nowDate = LocalDateTime.now();
        LambdaQueryWrapper<XunyeePic> picqw = new LambdaQueryWrapper<>();
        picqw.select(XunyeePic::getTitle, XunyeePic::getUrl)
                .eq(XunyeePic::getType_id, 3)//广告图
                .le(XunyeePic::getStart_time, nowDate)// >=
                .ge(XunyeePic::getFinish_time, nowDate);// <=
        XunyeePic xunyeePic = new XunyeePic().selectOne(picqw);
        ResPicTitleUrl titleUrl = new ResPicTitleUrl();
        if (xunyeePic != null) {
            titleUrl.setTitle(imageHostUtil.absImagePath(xunyeePic.getPic()));
            titleUrl.setUrl(xunyeePic.getUrl());
        }
        return R.OK(titleUrl);
    }

    /**
     * 会员一天 3次只能签一个人
     * 非会员一天 3人1次
     *
     * @param userId
     * @param personId
     * @return
     */
    public R vcuserPersonCheck(int userId, int personId) {

        Person person = metaService.getPersonById(personId);
        if (person == null || !person.getIs_xunyee_check()) {
            return R.ERROR("该艺人已关闭签到");
        }

        LocalDate gteDate = LocalDate.now(); // >=
        LocalDate ltDate = gteDate.plusDays(1); // <; // <

        LambdaQueryWrapper<XunyeeVcuserBenefit> qw = new LambdaQueryWrapper<>();
        qw.eq(XunyeeVcuserBenefit::getVcuser_id, userId)
                .le(XunyeeVcuserBenefit::getStart_time, gteDate)
                .ge(XunyeeVcuserBenefit::getFinish_time, gteDate);
        XunyeeVcuserBenefit vcuserBenefit = new XunyeeVcuserBenefit().selectOne(qw);

        // 查询当前用户关注的艺人和当天签到数
        List<ResMonUserPersonCheck> resPersonChecks = mongoTemplate.find(new Query(Criteria.where("vcuser")
                        .is(userId).and("updated")
                        .gte(gteDate).lt(ltDate)),
                ResMonUserPersonCheck.class);
        // 今日已使用次数
        int checkCounted = resPersonChecks.stream().collect(Collectors.summingInt(ResMonUserPersonCheck::getCheck));
        // 是否已为该艺人签到过

        boolean b = resPersonChecks.stream().anyMatch(task -> task.getPerson() == personId);
        if (b) {
            if (vcuserBenefit != null) {
                return R.ERROR("今天已经签到过了，明天再来吧");
            } else {
                return R.ERROR(ResultCode.USER_NOT_OPEN_VIP);
            }
        }

        if (checkCounted >= 3) {
            return R.ERROR("每天对所有艺人的签到数不能超过3。");
        }

        // 本次签到次数
        int checkCount = vcuserBenefit == null ? 1 : (3 - checkCounted);
        ReqMonUserPersonCheck reqCheck = new ReqMonUserPersonCheck();
        reqCheck.setVcuser(userId);
        reqCheck.setPerson(personId);
        reqCheck.setUpdated(LocalDateTime.now());
        reqCheck.setCheck(checkCount);
        ReqMonUserPersonCheck check = mongoTemplate.insert(reqCheck);
        if (check == null) {
            return R.ERROR("签到失败，请稍后再试");
        }


        // vc_user__person idol添加一条idol信息
        Update upUpdate = new Update();
        upUpdate.set("person", personId);
        upUpdate.set("vcuser", userId);
        upUpdate.set("updated", LocalDateTime.now());
        upUpdate.set("is_enabled", true);
        UpdateResult upResult = mongoTemplate.upsert(
                Query.query(Criteria.where("vcuser").is(userId)
                        .and("person").is(personId)),
                upUpdate, "vc_user__person");
        // vc_user__person idol添加一条idol信息

        // person__check__count 艺人签到数更新一条数据
        Update pUpdate = new Update();
        pUpdate.set("person", personId);
        pUpdate.set("data_time", LocalDate.now());
        pUpdate.inc("check", checkCount);//累加
        UpdateResult presult = mongoTemplate.upsert(Query.query(Criteria.where("person").is(personId)
                        .and("data_time").gte(LocalDate.now())),//大于等于当天的数据
                pUpdate,
                "person__check__count");
        // person__check__count 艺人签到数更新一条数据

        // 更新真爱排行
        Update update = new Update();
        update.set("vcuser", userId);
        update.set("person", personId);
        update.set("year", LocalDate.now().getYear());
        update.set("update", LocalDateTime.now());
        update.inc("check", checkCount);//累加
        UpdateResult result = mongoTemplate.upsert(Query.query(Criteria.where("vcuser").is(userId)
                        .and("person").is(personId)
                        .and("year").is(LocalDate.now().getYear())),
                update,
                "vc_user__person__check__count");
        // 更新真爱排行
        if (result.getModifiedCount() <= 0) {
            log.error("更新粉丝榜单失败,用户{}，艺人{}，签到数{}", userId, personId, checkCount);
        }

        return R.OK(checkCount);
    }

    public R<ResUserPersonCheckCalendar> vcuserPersonCheckCalendar(int userId, ReqPersonCheckCalendar req) {


        List<ResMonUserPersonCheckCalendar> countCalendars = mongoTemplate.find(Query.query(Criteria.where("vcuser").is(userId)
                        .and("person").is(req.getPerson())),
                ResMonUserPersonCheckCalendar.class);
        List<ResMonUserPersonCheckCalendar> yearCalenders = mongoTemplate.find(Query.query(
                Criteria.where("vcuser").is(userId)
                        .and("person").is(req.getPerson())
                        .andOperator(Criteria.where("updated")
                                .gte(DateUtil.getCurrYearFirst(LocalDate.now().getYear()))
                                .lt(DateUtil.getCurrYearLast(LocalDate.now().getYear())))),
                ResMonUserPersonCheckCalendar.class);
        //当前签到天数
        int count = countCalendars.size();
        // 今年签到次数
        int checkYear = yearCalenders.stream().mapToInt(ResMonUserPersonCheckCalendar::getCheck).sum();
        //当前月签到次数
        int checkMonth = 0;


        LocalDate ltDate = LocalDate.now(); // <; // <
        LocalDate gteDate = ltDate.minusDays(120); // >=
        if (StringUtils.isNotEmpty(req.getData_date())) {
            String[] dataDate = req.getData_date().split("-");
            int year = Integer.parseInt(dataDate[0]);
            int month = Integer.parseInt(dataDate[1]);
            ltDate = DateUtil.getLastDayOfMonth(year, month);
            gteDate = ltDate.minusMonths(1);


            checkMonth = (int) mongoTemplate.count(Query.query(Criteria.where("vcuser").is(userId)
                            .and("person").is(req.getPerson())
                            .andOperator(Criteria.where("updated").gte(gteDate).lt(ltDate))),
                    ResMonUserPersonCheckCalendar.class);
        } else {
            LocalDate mltDate = DateUtil.getLastDayOfMonth(LocalDate.now().getYear(), LocalDate.now().getMonthValue());
            LocalDate mgteDate = ltDate.minusMonths(1);

            checkMonth = (int) mongoTemplate.count(Query.query(Criteria.where("vcuser").is(userId)
                            .and("person").is(req.getPerson())
                            .and("updated").gte(mgteDate).lt(mltDate)),
                    ResMonUserPersonCheckCalendar.class);
        }
        Criteria criteria = Criteria.where("vcuser").is(userId)
                .and("person").is(req.getPerson())
                .andOperator(Criteria.where("updated").gte(gteDate).lt(ltDate));
        Query query = new Query(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "updated"));
        // 查询当前用户关给某个艺人和签到数
        List<ResMonUserPersonCheckCalendar> resMonUserPersonChecks = mongoTemplate.find(query, ResMonUserPersonCheckCalendar.class);

        // 签到日历数据
        List<ResUserPersonCheckCalendar.Result> results = new ArrayList<>();
        for (ResMonUserPersonCheckCalendar c : resMonUserPersonChecks) {
            ResUserPersonCheckCalendar.Result checkCalendar = new ResUserPersonCheckCalendar.Result();
            checkCalendar.setCheck(c.getCheck());
            checkCalendar.setDate(c.getUpdated());
            results.add(checkCalendar);
        }

        // 签到统计数据
        ResUserPersonCheckCalendar.CheckCount checkCount = new ResUserPersonCheckCalendar.CheckCount();
        checkCount.setMonth(checkMonth);
        checkCount.setYear(checkYear);
        // 日期数据
        ResUserPersonCheckCalendar.DateData dateData = new ResUserPersonCheckCalendar.DateData();
        dateData.setDate__gte(gteDate);
        dateData.setDate__lte(ltDate);

        ResUserPersonCheckCalendar checkCalendar = new ResUserPersonCheckCalendar();
        checkCalendar.setCount(count);
        checkCalendar.setResults(results);
        checkCalendar.setDate(dateData);
        checkCalendar.setCheck__count(checkCount);
        checkCalendar.setClosing_date(LocalDate.now());

        return R.OK(checkCalendar);
    }

    public R<ResPersonBrandInfo> vcuserPersonPersonBrand(int person) {
        List<ResBrandPersonList> brands = metaService.getPersonBrandList(person);
        int sale_rank = starService.getJDSaleRankByPerson(person);

        ResPersonBrandInfo brandInfo = new ResPersonBrandInfo();
        brandInfo.setClick(0);//这个不需要了 设置成0
        brandInfo.setSale_rank(sale_rank);
        brandInfo.setBrand_list(brands);
        return R.OK(brandInfo);

    }

    public R<ResPersonInfo> vcuserPersonPersonInfo(Integer userId, int person) {
        Person resPerson = metaService.getPersonById(person);
        if (resPerson == null) {
            return R.ERROR("艺人id错误");
        }

        // 当前艺人指数
        ResMonReportPersonRptTrend resMongo = mongoTemplate.findOne(Query.query(Criteria.where("person").is(person)), ResMonReportPersonRptTrend.class);

        ResPersonInfo info = new ResPersonInfo();
        info.setZh_name(resPerson.getZh_name());
        info.setPerson(resPerson.getId());
        info.setAvatar_custom(imageHostUtil.absImagePath(resPerson.getAvatar_custom()));
        info.setSex(resPerson.getSex() == 1 ? "男" : "女");
        if (resMongo != null) {
            info.setReport_1912_teleplay(resMongo.getReport_1912_teleplay());
            info.setReport_1912_teleplay_rank(resMongo.getReport_1912_teleplay_rank());
            info.setReport_1912_teleplay_rank_incr(resMongo.getReport_1912_teleplay_rank_incr());
        } else {
            // 没有指数设置成5
            info.setReport_1912_teleplay(5);
        }
        LocalDate nowDate = LocalDate.now();//今天
        // 当前艺人今日签到数
        ResMonPersonCheckCountCount personCheckCount = mongoTemplate.findOne(Query.query(Criteria.where("person").is(person)
                        .andOperator(Criteria.where("data_time").gte(nowDate))),
                ResMonPersonCheckCountCount.class);
        if (personCheckCount != null) {
            info.setCheck(personCheckCount.getCheck());
        }
        // 查询当前用户关注的艺人和当天签到数
        if (userId != null) {
            ResMonUserPersonCheck userPersonCheck = mongoTemplate.findOne(Query.query(Criteria.where("vcuser").is(userId)
                            .and("person").is(person)
                            .andOperator(Criteria.where("updated").gte(nowDate))),
                    ResMonUserPersonCheck.class);
            if (userPersonCheck != null) {
                info.setCheck_my(userPersonCheck.getCheck());
            }
        }
        return R.OK(info);
    }

    public R<List<ResPersonCurve>> reportPersonRptTrendAll(ReqPersonQuxian req) {
        // 当前艺人指数
        LocalDate gteDate = LocalDate.now().minusDays(10);
        LocalDate ltDate = LocalDate.now();
        // 查询条件
        Criteria criteria = Criteria.where("is_eighty").is(req.getIs_eighty()).and("person").in(req.getPerson())
                .andOperator(Criteria.where("start_data_time").gte(gteDate).lt(ltDate));
        Query query = new Query();
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.ASC, "start_data_time"));
        List<ResMonReportAllPersonRptTrend> resMongo = mongoTemplate.find(query, ResMonReportAllPersonRptTrend.class);

        List<ResPersonCurve> quxes = new ArrayList<>();
        for (ResMonReportAllPersonRptTrend mongo : resMongo) {
            ResPersonCurve qux = new ResPersonCurve();
            qux.setReport_1912_teleplay(mongo.getReport_1912_teleplay());
            qux.setReport_1912_teleplay_rank(mongo.getReport_1912_teleplay_rank());
            qux.setData_time(mongo.getStart_data_time().toLocalDate());
            quxes.add(qux);
        }
        return R.OK(quxes);
    }

    public R<List<ResPersonFansRank>> reportPersonRptFansRank(Integer person) {
        int nowYear = LocalDate.now().getYear();

        // 查询条件
        Query query = new Query();
        Criteria criteria = Criteria.where("person").is(person).and("year").is(nowYear);
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "check"));
        query.limit(40);
        List<ResMonUserPersonCheckCount> resMon = mongoTemplate.find(query, ResMonUserPersonCheckCount.class);

        List<ResPersonFansRank.Fans> fans = new ArrayList<>();
        if (resMon.size() > 0) {
            Integer[] vcuserIds = resMon.stream().map(e -> e.getVcuser()).collect(Collectors.toList())
                    .toArray(new Integer[resMon.size()]);
            QueryWrapper qw = new QueryWrapper();
            qw.in("id", vcuserIds);
            List<XunyeeVcuser> vcusers = new XunyeeVcuser().selectList(qw);
            for (XunyeeVcuser vcuser : vcusers) {
                ResPersonFansRank.Fans fansRank = new ResPersonFansRank.Fans();
                fansRank.setAvatar(imageHostUtil.absImagePath(vcuser.getAvatar()));
                fansRank.setVcuser_id(vcuser.getId());
                fansRank.setNickname(vcuser.getNickname());
                fans.add(fansRank);
            }
            for (ResPersonFansRank.Fans fansRank : fans) {
                for (ResMonUserPersonCheckCount personCheckCount : resMon) {
                    if (fansRank.getVcuser_id() == personCheckCount.getVcuser()) {
                        fansRank.setCheck(personCheckCount.getCheck());
                        break;
                    }
                }
            }
        }


        ResPersonFansRank fansRank = new ResPersonFansRank();
        fansRank.setYear(nowYear);
        fansRank.setEnd_date(LocalDate.now());
        fansRank.setFans(fans);
        return R.OK(fansRank);

    }

    public R reportPersonAlbum(ReqMyPage myPage, int person) {
        IPage iPage = starService.getPersonGalleryByPersonId(person, myPage);
        List<SdbPersonGallery> resBlogPages = iPage.getRecords();
        List<String> list = new ArrayList<>();
        for (SdbPersonGallery resBlogPage : resBlogPages) {
            list.add(imageHostUtil.absImagePath(resBlogPage.getOriginal()));
        }
        Map map = new HashMap();
        map.put("person", person);
        map.put("count", iPage.getTotal());
        map.put("album_list", list);
        return R.OK(map);
    }


    @Transactional
    public R vcuserBenefitVoucher(int userId, String voucher) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq("voucher", voucher);
        XunyeeVcuserVoucher vcuserVoucher = new XunyeeVcuserVoucher().selectOne(qw);
        if (vcuserVoucher == null) {
            return R.ERROR("兑换码错误");
        }
        if (!vcuserVoucher.getIs_enabled()) {
            return R.ERROR("兑换码已使用");
        }


        XunyeeBenefitPrice benefitPrice = new XunyeeBenefitPrice().selectById(vcuserVoucher.getBenefit_price_id());
        if (benefitPrice == null || !benefitPrice.getIs_enabled()) {
            return R.ERROR("兑换码对应的活动不存在");
        }


        LocalDate nowDate = LocalDate.now();

        LambdaQueryWrapper<XunyeeVcuserBenefit> bqw = new LambdaQueryWrapper();
        bqw.eq(XunyeeVcuserBenefit::getVcuser_id, userId)
                .ge(XunyeeVcuserBenefit::getFinish_time, nowDate)
                .orderByDesc(XunyeeVcuserBenefit::getFinish_time)
                .last("limit 1");
        XunyeeVcuserBenefit temp = new XunyeeVcuserBenefit().selectOne(bqw);
        int benefitId = benefitPrice.getBenefit_id();
        int plusDays = benefitPrice.getQuantity();
        Date date = new Date();
        if (temp == null) {
            XunyeeVcuserBenefit vcuserBenefit = new XunyeeVcuserBenefit();
            vcuserBenefit.setId(UUID.randomUUID().toString());
            vcuserBenefit.setBenefit_id(benefitId);
            vcuserBenefit.setVcuser_id(userId);
            vcuserBenefit.setUpdated(date);
            vcuserBenefit.setCreated(date);
            vcuserBenefit.setStart_time(nowDate);
            vcuserBenefit.setFinish_time(nowDate.plusDays(plusDays));
            if (!vcuserBenefit.insert()) {
                return R.ERROR();
            }
        } else {
            // 这里使用xunyeeVcuserBenefitMapper 是因为 pgsql的主键使用的是uuid类型，不能updateById;
            temp.setBenefit_id(benefitId);
            temp.setUpdated(date);
            // 结束时间延长
            temp.setFinish_time(temp.getFinish_time().plusDays(plusDays));
            LambdaUpdateWrapper<XunyeeVcuserBenefit> updateQw = new LambdaUpdateWrapper<>();
            updateQw.eq(XunyeeVcuserBenefit::getId, UUID.fromString(temp.getId()));
            int updatedCount=xunyeeVcuserBenefitMapper.update(temp,updateQw);
            if (updatedCount<=0) {
                return R.ERROR();
            }
        }

        vcuserVoucher.setIs_enabled(false);
        vcuserVoucher.setVcuser_id(userId);
        vcuserVoucher.setUpdated(date);
        if (!vcuserVoucher.updateById()) {
            return R.ERROR();
        }


        return R.OK();
    }

    public R<WxPayAppOrderResult> vcuserBenefitPayOrderSubmit(HttpServletRequest request, int userId, ReqBenefitPayOrder req) {
        int site = req.getSite();

        XunyeeBenefitPrice price = new XunyeeBenefitPrice().selectById(req.getBenefit_price());
        if (price == null) {
            return R.ERROR("您购买的会员服务不存在");
        }
        // 生成一条付款记录 状态是未支付
        XunyeeVcuserBenefitPayorder payorder = new XunyeeVcuserBenefitPayorder();
        payorder.setVcuser_id(userId);
        payorder.setBenefit_price_id(price.getId());
        payorder.setIs_paid(false);
        payorder.setQuantity(price.getQuantity());
        payorder.setSite(site);
        payorder.setPrice(price.getPrice());
        String orderNo = OrderCodeFactory.getOrderCode((long) userId);
        payorder.setSite_transaction_id(orderNo);
        Date nowDate = new Date();
        payorder.setUpdated(nowDate);
        payorder.setCreated(nowDate);
        if (payorder.insert()) {
            return payService.payBenefit(request, payorder);
        }
        return R.ERROR("下单失败");
    }

    public R<Map<String, Object>> globalSearch(Integer userId, ReqMyPage myPage, ReqGlobalSearch reqGlobalSearch) {

        String keyword = reqGlobalSearch.getName();
        //List<ResPerson> persons = metaService.getPersonLimit(keyword, 3);

        //=======================  按签到排名来搜索 =============================
        LocalDate nowDate = LocalDate.now();//今天
        LocalDate gteDate = LocalDate.now();
        LocalDate ltDate = gteDate.plusDays(1);
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
                .filter(checkCount -> checkCount.getZh_name().contains(keyword))
                .sorted(Comparator.comparing(ResPersonCheckCount::getCheck).reversed())// 按签到数 倒叙
                .collect(Collectors.toList());

        List<ResPerson> personList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ResPerson resPerson = new ResPerson();
            resPerson.setId(resSortCheckCounts.get(i).getId());
            resPerson.setAvatar_custom(resSortCheckCounts.get(i).getAvatar_custom());
            resPerson.setZh_name(resSortCheckCounts.get(i).getZh_name());
            personList.add(resPerson);
        }

        Page page = new Page(myPage.getCurrent(), myPage.getSize());
        IPage<ResBlogPage> iPage = myMapper.selectBlogBySearch(page, keyword, userId);
        for (ResBlogPage record : iPage.getRecords()) {
            if (StringUtils.isNotEmpty(record.getCover())) {
                record.setCover(imageHostUtil.absImagePath(record.getCover()));
            }
        }
        //=======================  按签到排名来搜索 =============================


        Map<String, Object> map = new HashMap<>();
        map.put("persons", personList);
        map.put("blog_page", iPage);
        return R.OK(map);
    }

    public R<IPage<ResBlogPage>> blogSearch(Integer userId, ReqMyPage myPage, ReqGlobalSearch reqGlobalSearch) {
        String keyword = reqGlobalSearch.getName();
        Page page = new Page(myPage.getCurrent(), myPage.getSize());
        IPage<ResBlogPage> iPage = myMapper.selectBlogBySearch(page, keyword, userId);
        for (ResBlogPage record : iPage.getRecords()) {
            if (StringUtils.isNotEmpty(record.getCover())) {
                record.setCover(imageHostUtil.absImagePath(record.getCover()));
            }
        }
        return R.OK(iPage);
    }

    public R agreement(int type) throws IOException {
        if (type == 1) {
            String str = "<!DOCTYPE html>" +
                    "            <html>" +
                    "                <head>" +
                    "                    <title>隐私政策</title>" +
                    "                    <meta charset=\"utf-8\" />" +
                    "                    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
                    "                    <meta name=\"bear-note-unique-identifier\" content=\"59C91F93-5C22-4F88-BF06-B898E18750C8-53752-0000D7D085DC104C\">" +
                    "                    <meta name=\"created\" content=\"2020-10-29T12:21:30+0800\"/>" +
                    "                    <meta name=\"modified\" content=\"2020-10-29T12:40:23+0800\"/>" +
                    "                    <meta name=\"tags\" content=\"\"/>" +
                    "                    <meta name=\"last device\" content=\"Rorschach\"/>" +
                    "                </head>" +
                    "                <body>" +
                    "                    <div class=\"note-wrapper\">" +
                    "                        <h1 id=\"隐私政策\">隐私政策</h1>" +
                    "            <p>上海纬岭文化传播有限公司(简称“我们”)作为寻艺的运营者，我们重视用户的隐私。您在使用我们的服务时，我们可能会收集和使用您的相关信息。我们希望通过本《隐私政策》向您说明，在使用我们的服务时，我们如何收集、使用、储存和分享这些信息，以及我们为您提供的访问、更新、控制和保护这些信息的方式。本《隐私政策》与您所使用的服务息息相关，希望您仔细阅读，在需要时，按照本《隐私政策》的指引，作出您认为适当的选择。本《隐私政策》中涉及的相关技术词汇，我们尽量以简明扼要的表述，并提供进一步说明的链接，以便您的理解。</p>" +
                    "            <br>" +
                    "            <p>您使用或继续使用我们的服务，即意味着同意我们按照本《隐私政策》收集、使用、储存和分享您的相关信息。</p>" +
                    "            <br>" +
                    "            <p>如对本《隐私政策》或相关事宜有任何问题，请通过<a href=\"mailto:service@vlinkage.com\">service@vlinkage.com</a>与我们联系。</p>" +
                    "            <br>" +
                    "            <br>" +
                    "            <h2 id=\"我们可能收集的信息\">我们可能收集的信息</h2>" +
                    "            <p>我们提供服务时，可能会收集、储存和使用下列与您有关的信息。如果您不提供相关信息，可能无法享受我们提供的某些服务，或者无法达到相关服务拟达到的效果。</p>" +
                    "            <br>" +
                    "            <br>" +
                    "            <h3 id=\"您提供的信息\">您提供的信息</h3>" +
                    "            <p>您在使用我们的服务时，向我们提供的相关个人信息，例如电话号码、电子邮件或银行卡号等；</p>" +
                    "            <br>" +
                    "            <p>您通过我们的服务向其他方提供的共享信息，以及您使用我们的服务时所储存的信息。</p>" +
                    "            <br>" +
                    "            <br>" +
                    "            <h3 id=\"其他方分享的您的信息\">其他方分享的您的信息</h3>" +
                    "            <p>其他方使用我们的服务时所提供有关您的共享信息。</p>" +
                    "            <br>" +
                    "            <br>" +
                    "            <h3 id=\"我们获取的您的信息\">我们获取的您的信息</h3>" +
                    "            <p>您使用服务时我们可能收集如下信息：</p>" +
                    "            <br>" +
                    "            <p>日志信息，指您使用我们的服务时，系统可能通过cookies、web beacon或其他方式自动采集的技术信息，包括：</p>" +
                    "            <br>" +
                    "            <p>设备或软件信息，例如您的移动设备、网页浏览器或用于接入我们服务的其他程序所提供的配置信息、您的IP地址和移动设备所用的版本和设备识别码；</p>" +
                    "            <br>" +
                    "            <p>在使用我们服务时搜索或浏览的信息，例如您使用的网页搜索词语、访问的社交媒体页面url地址，以及您在使用我们服务时浏览或要求提供的其他信息和内容详情；</p>" +
                    "            <br>" +
                    "            <p>有关您曾使用的移动应用（APP）和其他软件的信息，以及您曾经使用该等移动应用和软件的信息；</p>" +
                    "            <br>" +
                    "            <p>位置信息，指您开启设备定位功能并使用我们基于位置提供的相关服务时，收集的有关您位置的信息，包括：</p>" +
                    "            <br>" +
                    "            <p>您通过具有定位功能的移动设备使用我们的服务时，通过GPS或WiFi等方式收集的您的地理位置信息；</p>" +
                    "            <br>" +
                    "            <p>您或其他用户提供的包含您所处地理位置的实时信息，例如您提供的账户信息中包含的您所在地区信息，您或其他人上传的显示您当前或曾经所处地理位置的共享信息；</p>" +
                    "            <br>" +
                    "            <p>您可以通过关闭定位功能，停止对您的地理位置信息的收集。</p>" +
                    "            <br>" +
                    "            <br>" +
                    "            <h2 id=\"我们可能如何使用信息\">我们可能如何使用信息</h2>" +
                    "            <p>我们可能将在向您提供服务的过程之中所收集的信息用作下列用途：</p>" +
                    "            <br>" +
                    "            <h3 id=\"向您提供服务；\">向您提供服务；</h3>" +
                    "            <p>在我们提供服务时，用于身份验证、客户服务、安全防范、诈骗监测、存档和备份用途，确保我们向您提供的产品和服务的安全性；</p>" +
                    "            <br>" +
                    "            <br>" +
                    "            <h3 id=\"帮助我们设计新服务，改善我们现有服务；\">帮助我们设计新服务，改善我们现有服务；</h3>" +
                    "            <p>使我们更加了解您如何接入和使用我们的服务，从而针对性地回应您的个性化需求，例如语言设定、位置设定、个性化的帮助服务和指示，或对您和其他用户作出其他方面的回应；</p>" +
                    "            <br>" +
                    "            <br>" +
                    "            <h3 id=\"向您提供与您更加相关的广告以替代普遍投放的广告；\">向您提供与您更加相关的广告以替代普遍投放的广告；</h3>" +
                    "            <p>评估我们服务中的广告和其他促销及推广活动的效果，并加以改善；</p>" +
                    "            <br>" +
                    "            <br>" +
                    "            <h3 id=\"软件认证或管理软件升级；\">软件认证或管理软件升级；</h3>" +
                    "            <p>让您参与有关我们产品和服务的调查。</p>" +
                    "            <br>" +
                    "            <p>为了让您有更好的体验、改善我们的服务或您同意的其他用途，在符合相关法律法规的前提下，我们可能将通过某一项服务所收集的信息，以汇集信息或者个性化的方式，用于我们的其他服务。例如，在您使用我们的一项服务时所收集的信息，可能在另一服务中用于向您提供特定内容，或向您展示与您相关的、非普遍推送的信息。如果我们在相关服务中提供了相应选项，您也可以授权我们将该服务所提供和储存的信息用于我们的其他服务。</p>" +
                    "            <br>" +
                    "            <br>" +
                    "            <h2 id=\"您如何访问和控制自己的个人信息\">您如何访问和控制自己的个人信息</h2>" +
                    "            <p>我们将尽一切可能采取适当的技术手段，保证您可以访问、更新和更正自己的注册信息或使用我们的服务时提供的其他个人信息。在访问、更新、更正和删除前述信息时，我们可能会要求您进行身份验证，以保障账户安全。</p>" +
                    "            <br>" +
                    "            <br>" +
                    "            <h2 id=\"我们可能分享的信息\">我们可能分享的信息</h2>" +
                    "            <p>除以下情形外，未经您同意，我们不会与任何第三方分享您的个人信息：</p>" +
                    "            <br>" +
                    "            <p>我们可能将您的个人信息与第三方服务供应商、承包商及代理（例如代表我们发出电子邮件或推送通知的通讯服务提供商、为我们提供位置数据的地图服务供应商）分享（他们可能并非位于您所在的法域），用作下列用途：</p>" +
                    "            <br>" +
                    "            <p>向您提供我们的服务；</p>" +
                    "            <br>" +
                    "            <p>实现“我们可能如何使用信息”部分所述目的；</p>" +
                    "            <br>" +
                    "            <p>履行我们在本《隐私政策》中的义务和行使我们的权利；</p>" +
                    "            <br>" +
                    "            <p>理解、维护和改善我们的服务。</p>" +
                    "            <br>" +
                    "            <p>如我们与任何上述第三方分享您的个人信息，我们将努力确保该等第三方在使用您的个人信息时遵守本《隐私政策》及我们要求其遵守的其他适当的保密和安全措施。</p>" +
                    "            <br>" +
                    "            <p>随着我们业务的持续发展，我们有可能进行合并、收购、资产转让或类似的交易，您的个人信息有可能作为此类交易的一部分而被转移。我们将在转移前通知您。</p>" +
                    "            <br>" +
                    "            <p>我们还可能为以下需要而保留、保存或披露您的个人信息：</p>" +
                    "            <br>" +
                    "            <p>遵守适用的法律法规；</p>" +
                    "            <br>" +
                    "            <p>遵守法院命令或其他法律程序的规定；</p>" +
                    "            <br>" +
                    "            <p>遵守相关政府机关的要求；</p>" +
                    "            <br>" +
                    "            <p>为遵守适用的法律法规、维护社会公共利益，或保护我们的客户、我们、其他用户的人身和财产安全或合法权益所合理必需的用途。</p>" +
                    "            <br>" +
                    "            <br>" +
                    "            <h2 id=\"信息安全\">信息安全</h2>" +
                    "            <p>我们仅在本《隐私政策》所述目的所必需的期间和法律法规要求的时限内保留您的个人信息。 我们使用各种安全技术和程序，以防信息的丢失、不当使用、未经授权阅览或披露。例如，在某些服务中，我们将利用加密技术（例如SSL）来保护您提供的个人信息。但请您理解，由于技术的限制以及可能存在的各种恶意手段，在互联网行业，即便竭尽所能加强安全措施，也不可能始终保证信息百分之百的安全。您需要了解，您接入我们的服务所用的系统和通讯网络，有可能因我们可控范围外的因素而出现问题。</p>" +
                    "            <br>" +
                    "            <br>" +
                    "            <h2 id=\"您分享的信息\">您分享的信息</h2>" +
                    "            <p>我们的多项服务，可让您不仅与自己的社交网络，也与使用该服务的所有用户公开分享您的相关信息，例如，您在我们的服务中所上传或发布的信息（包括您公开的个人信息、您建立的名单）、您对其他人上传或发布的信息作出的回应，以及包括与这些信息有关的位置数据和日志信息。使用我们服务的其他用户也有可能分享与您有关的信息（包括位置数据和日志信息）。特别是，我们的社交媒体服务，是专为使您与世界各地的用户共享信息而设计，您可以使共享信息实时、广泛地传递。只要您不删除共享信息，有关信息会一直留存在公共领域；即使您删除共享信息，有关信息仍可能由其他用户或不受我们控制的非关联第三方独立地缓存、复制或储存，或由其他用户或该等第三方在公共领域保存。</p>" +
                    "            <br>" +
                    "            <p>因此，请您谨慎考虑通过我们的服务上传、发布和交流的信息内容。在一些情况下，您可通过我们某些服务的隐私设定来控制有权浏览您共享信息的用户范围。如要求从我们的服务中删除您的相关信息，请通过该等特别服务条款提供的方式操作。</p>" +
                    "            <br>" +
                    "            <br>" +
                    "            <h2 id=\"您分享的敏感个人信息\">您分享的敏感个人信息</h2>" +
                    "            <p>某些个人信息因其特殊性可能被认为是敏感个人信息，例如您的种族、宗教、个人健康和医疗信息等。相比其他个人信息，敏感个人信息受到更加严格的保护。</p>" +
                    "            <br>" +
                    "            <p>请注意，您在使用我们的服务时所提供、上传或发布的内容和信息（例如有关您社交活动的照片等信息），可能会泄露您的敏感个人信息。您需要谨慎地考虑，是否在使用我们的服务时披露相关敏感个人信息。</p>" +
                    "            <br>" +
                    "            <p>您同意按本《隐私政策》所述的目的和方式来处理您的敏感个人信息。</p>" +
                    "            <br>" +
                    "            <br>" +
                    "            <h2 id=\"我们可能如何收集信息\">我们可能如何收集信息</h2>" +
                    "            <p>我们或我们的第三方合作伙伴，可能通过cookies和web beacon收集和使用您的信息，并将该等信息储存为日志信息。</p>" +
                    "            <br>" +
                    "            <p>我们使用自己的cookies和web beacon，目的是为您提供更个性化的用户体验和服务，并用于以下用途：</p>" +
                    "            <br>" +
                    "            <p>记住您的身份。例如：cookies和web beacon有助于我们辨认您作为我们的注册用户的身份，或保存您向我们提供的有关您的喜好或其他信息；</p>" +
                    "            <br>" +
                    "            <p>分析您使用我们服务的情况。例如，我们可利用cookies和web beacon来了解您使用我们的服务进行什么活动，或哪些网页或服务最受您的欢迎；</p>" +
                    "            <br>" +
                    "            <p>广告优化。Cookies和web beacon有助于我们根据您的信息，向您提供与您相关的广告而非进行普遍的广告投放。</p>" +
                    "            <br>" +
                    "            <p>我们为上述目的使用cookies和web beacon的同时，可能将通过cookies和web beacon收集的非个人身份信息，经统计加工后提供给广告商或其他合作伙伴，用于分析用户如何使用我们的服务，并用于广告服务。</p>" +
                    "            <br>" +
                    "            <p>我们的产品和服务上可能会有广告商或其他合作方放置的cookies和web beacon。这些cookies和web beacon可能会收集与您相关的非个人身份信息，以用于分析用户如何使用该等服务、向您发送您可能感兴趣的广告，或用于评估广告服务的效果。这些第三方cookies和web beacon收集和使用该等信息，不受本《隐私政策》约束，而是受相关使用者的隐私政策约束，我们不对第三方的cookies或web beacon承担责任。</p>" +
                    "            <br>" +
                    "            <p>您可以通过浏览器设置拒绝或管理cookies或web beacon。但请注意，如果停用cookies或web beacon，您有可能无法享受最佳的服务体验，某些服务也可能无法正常使用。同时，您还会收到同样数量的广告，但这些广告与您的相关性会降低。</p>" +
                    "            <br>" +
                    "            <br>" +
                    "            <h2 id=\"广告服务\">广告服务</h2>" +
                    "            <p>我们可能使用您的相关信息，向您提供与您更加相关的广告。</p>" +
                    "            <br>" +
                    "            <p>我们也可能使用您的信息，通过我们的服务、电子邮件或其他方式向您发送营销信息，提供或推广我们或第三方的如下商品和服务：</p>" +
                    "            <br>" +
                    "            <p>我们的服务，我们的关联公司和合作伙伴的商品或服务，包括即时通讯服务、网上媒体服务、互动娱乐服务、社交网络服务、付款服务、互联网搜索服务、位置和地图服务、应用软件和服务、数据管理软件和服务、网上广告服务、互联网金融，以及其他社交媒体、娱乐、电子商务、资讯和通讯软件或服务（统称“互联网服务”）；</p>" +
                    "            <br>" +
                    "            <p>第三方互联网服务供应商，以及与下列有关的第三方商品或服务：食物和餐饮、体育、音乐、电影、电视、现场表演及其他艺术和娱乐、书册、杂志和其他刊物、服装和配饰、珠宝、化妆品、个人健康和卫生、电子、收藏品、家用器皿、电器、家居装饰和摆设、宠物、汽车、酒店、交通和旅游、银行、保险及其他金融服务、会员积分和奖励计划，以及我们认为可能与您相关的其他商品或服务。</p>" +
                    "            <br>" +
                    "            <p>如您不希望我们将您的个人信息用作前述广告用途，您可以通过我们在广告中提供的相关提示，或在特定服务中提供的指引，要求我们停止为上述用途使用您的个人信息。</p>" +
                    "            <br>" +
                    "            <br>" +
                    "            <h2 id=\"我们可能向您发送的邮件和信息\">我们可能向您发送的邮件和信息</h2>" +
                    "            <br>" +
                    "            <h3 id=\"邮件和信息推送\">邮件和信息推送</h3>" +
                    "            <p>您在使用我们的服务时，我们可能使用您的信息向您的设备发送电子邮件、新闻或推送通知。如您不希望收到这些信息，可以按照我们的相关提示，在设备上选择取消订阅。</p>" +
                    "            <br>" +
                    "            <br>" +
                    "            <h3 id=\"与服务有关的公告\">与服务有关的公告</h3>" +
                    "            <p>我们可能在必要时（例如因系统维护而暂停某一项服务时）向您发出与服务有关的公告。您可能无法取消这些与服务有关、性质不属于推广的公告。</p>" +
                    "            <br>" +
                    "            <br>" +
                    "            <h2 id=\"隐私政策的适用例外\">隐私政策的适用例外</h2>" +
                    "            <p>我们的服务可能包括或链接至第三方提供的社交媒体或其他服务（包括网站）。例如：</p>" +
                    "            <br>" +
                    "            <p>您利用 “分享”键将某些内容分享到我们的服务，或您利用第三方连线服务登录我们的服务。这些功能可能会收集您的相关信息（包括您的日志信息），并可能在您的电脑装置cookies，从而正常运行上述功能；</p>" +
                    "            <br>" +
                    "            <p>我们通过广告或我们服务的其他方式向您提供链接，使您可以接入第三方的服务或网站。</p>" +
                    "            <br>" +
                    "            <p>该等第三方社交媒体或其他服务可能由相关的第三方或我们运营。您使用该等第三方的社交媒体服务或其他服务（包括您向该等第三方提供的任何个人信息），须受该第三方的服务条款及隐私政策（而非《通用服务条款》或本《隐私政策》）约束，您需要仔细阅读其条款。本《隐私政策》仅适用于我们所收集的信息，并不适用于任何第三方提供的服务或第三方的信息使用规则，我们对任何第三方使用由您提供的信息不承担任何责任。</p>" +
                    "            <br>" +
                    "            <br>" +
                    "            <h2 id=\"未成年人使用我们的服务\">未成年人使用我们的服务</h2>" +
                    "            <p>我们鼓励父母或监护人指导未满十八岁的未成年人使用我们的服务。我们建议未成年人鼓励他们的父母或监护人阅读本《隐私政策》，并建议未成年人在提交的个人信息之前寻求父母或监护人的同意和指导。</p>" +
                    "            <br>" +
                    "            <br>" +
                    "            <h2 id=\"隐私政策的适用范围\">隐私政策的适用范围</h2>" +
                    "            <p>除某些特定服务外，我们所有的服务均适用本《隐私政策》。这些特定服务将适用特定的隐私政策。针对某些特定服务的特定隐私政策，将更具体地说明我们在该等服务中如何使用您的信息。该特定服务的隐私政策构成本《隐私政策》的一部分。如相关特定服务的隐私政策与本《隐私政策》有不一致之处，适用该特定服务的隐私政策。</p>" +
                    "            <br>" +
                    "            <p>请您注意，本《隐私政策》不适用于以下情况：</p>" +
                    "            <br>" +
                    "            <p>通过我们的服务而接入的第三方服务（包括任何第三方网站）收集的信息；</p>" +
                    "            <br>" +
                    "            <p>通过在我们服务中进行广告服务的其他公司或机构所收集的信息。</p>" +
                    "            <br>" +
                    "            <br>" +
                    "            <h2 id=\"变更\">变更</h2>" +
                    "            <p>我们可能适时修订本《隐私政策》的条款，该等修订构成本《隐私政策》的一部分。如该等修订造成您在本《隐私政策》下权利的实质减少，我们将在修订生效前通过在主页上显著位置提示或向您发送电子邮件或以其他方式通知您。在该种情况下，若您继续使用我们的服务，即表示同意受经修订的本《隐私政策》的约束。</p>" +
                    "                    </div>" +
                    "                    <script type=\"text/javascript\">" +
                    "                        (function() {" +
                    "" +
                    "                var doc_ols = document.getElementsByTagName(\"ol\");" +
                    "" +
                    "                for ( i=0; i<doc_ols.length; i++) {" +
                    "" +
                    "                    var ol_start = doc_ols[i].getAttribute(\"start\") - 1;" +
                    "                    doc_ols[i].setAttribute(\"style\", \"counter-reset:ol_counter \" + ol_start + \";\");" +
                    "" +
                    "                }" +
                    "" +
                    "            })();" +
                    "" +
                    "                    </script>" +
                    "                    <style>" +
                    "                        html,body,div,span,applet,object,iframe,h1,h2,h3,h4,h5,h6,p,blockquote,pre,a,abbr,acronym,address,big,cite,code,del,dfn,em,img,ins,kbd,q,s,samp,small,strike,strong,sub,sup,tt,var,b,u,i,center,dl,dt,dd,ol,ul,li,fieldset,form,label,legend,table,caption,tbody,tfoot,thead,tr,th,td,article,aside,canvas,details,embed,figure,figcaption,footer,header,hgroup,menu,nav,output,ruby,section,summary,time,mark,audio,video{margin:0;padding:0;border:0;font:inherit;font-size:100%;vertical-align:baseline}html{line-height:1}ol,ul{list-style:none}table{border-collapse:collapse;border-spacing:0}caption,th,td{text-align:left;font-weight:normal;vertical-align:middle}q,blockquote{quotes:none}q:before,q:after,blockquote:before,blockquote:after{content:\"\";content:none}a img{border:none}article,aside,details,figcaption,figure,footer,header,hgroup,main,menu,nav,section,summary{display:block}*{-moz-box-sizing:border-box;-webkit-box-sizing:border-box;box-sizing:border-box}html{font-size:87.5%;line-height:1.57143em}html{font-size:14px;line-height:1.6em;-webkit-text-size-adjust:100%}body{background:#fcfcfc;color:#545454;text-rendering:optimizeLegibility;font-family:\"AvenirNext-Regular\"}a{color:#de4c4f;text-decoration:none}h1{font-family:\"AvenirNext-Medium\";color:#333;font-size:1.6em;line-height:1.3em;margin-bottom:.78571em}h2{font-family:\"AvenirNext-Medium\";color:#333;font-size:1.3em;line-height:1em;margin-bottom:.62857em}h3{font-family:\"AvenirNext-Medium\";color:#333;font-size:1.15em;line-height:1em;margin-bottom:.47143em}p{margin-bottom:1.57143em;hyphens:auto}hr{height:1px;border:0;background-color:#dedede;margin:-1px auto 1.57143em auto}ul,ol{margin-bottom:.31429em}ul ul,ul ol,ol ul,ol ol{margin-bottom:0px}ol{counter-reset:ol_counter}ol li:before{content:counter(ol_counter) \".\";counter-increment:ol_counter;color:#e06e73;text-align:right;display:inline-block;min-width:1em;margin-right:0.5em}b,strong{font-family:\"AvenirNext-Bold\"}i,em{font-family:\"AvenirNext-Italic\"}code{font-family:\"Menlo-Regular\"}.text-overflow-ellipsis{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.sf_code_string,.sf_code_selector,.sf_code_attr-name,.sf_code_char,.sf_code_builtin,.sf_code_inserted{color:#D33905}.sf_code_comment,.sf_code_prolog,.sf_code_doctype,.sf_code_cdata{color:#838383}.sf_code_number,.sf_code_boolean{color:#0E73A2}.sf_code_keyword,.sf_code_atrule,.sf_code_rule,.sf_code_attr-value,.sf_code_function,.sf_code_class-name,.sf_code_class,.sf_code_regex,.sf_code_important,.sf_code_variable,.sf_code_interpolation{color:#0E73A2}.sf_code_property,.sf_code_tag,.sf_code_constant,.sf_code_symbol,.sf_code_deleted{color:#1B00CE}.sf_code_macro,.sf_code_entity,.sf_code_operator,.sf_code_url{color:#920448}.note-wrapper{max-width:46em;margin:0px auto;padding:1.57143em 3.14286em}.note-wrapper.spotlight-preview{overflow-x:hidden}u{text-decoration:none;background-image:linear-gradient(to bottom, rgba(0,0,0,0) 50%,#e06e73 50%);background-repeat:repeat-x;background-size:2px 2px;background-position:0 1.05em}s{color:#878787}p{margin-bottom:0.1em}hr{margin-bottom:0.7em;margin-top:0.7em}ul li{text-indent:-0.35em}ul li:before{content:\"•\";color:#e06e73;display:inline-block;margin-right:0.3em}ul ul{margin-left:1.25714em}ol li{text-indent:-1.45em}ol ol{margin-left:1.25714em}blockquote{display:block;margin-left:-1em;padding-left:0.8em;border-left:0.2em solid #e06e73}.todo-list ul{margin-left:1.88571em}.todo-list li{text-indent:-1.75em}.todo-list li:before{content:\"\";display:static;margin-right:0px}.todo-checkbox{text-indent:-1.7em}.todo-checkbox svg{margin-right:0.3em;position:relative;top:0.2em}.todo-checkbox svg #check{display:none}.todo-checkbox.todo-checked #check{display:inline}.todo-checkbox.todo-checked+.todo-text{text-decoration:line-through;color:#878787}.code-inline{display:inline;background:white;border:solid 1px #dedede;padding:0.2em 0.5em;font-size:0.9em}.code-multiline{display:block;background:white;border:solid 1px #dedede;padding:0.7em 1em;font-size:0.9em;overflow-x:auto}.hashtag{display:inline-block;color:white;background:#b8bfc2;padding:0.0em 0.5em;border-radius:1em;text-indent:0}.hashtag a{color:#fff}.address a{color:#545454;background-image:linear-gradient(to bottom, rgba(0,0,0,0) 50%,#0da35e 50%);background-repeat:repeat-x;background-size:2px 2px;background-position:0 1.05em}.address svg{position:relative;top:0.2em;display:inline-block;margin-right:0.2em}.color-preview{display:inline-block;width:1em;height:1em;border:solid 1px rgba(0,0,0,0.3);border-radius:50%;margin-right:0.1em;position:relative;top:0.2em;white-space:nowrap}.color-code{margin-right:0.2em;font-family:\"Menlo-Regular\";font-size:0.9em}.color-hash{opacity:0.4}.ordered-list-number{color:#e06e73;text-align:right;display:inline-block;min-width:1em}.arrow svg{position:relative;top:0.08em;display:inline-block;margin-right:0.15em;margin-left:0.15em}.arrow svg #rod{stroke:#545454}.arrow svg #point{fill:#545454}mark{color:inherit;display:inline;padding:0.2em 0.5em;background-color:#fcffc0}img{max-width:100%;height:auto}" +
                    "" +
                    "                    </style>" +
                    "                </body>" +
                    "            </html>";
            return R.OK(str);
        } else if (type == 2) {
            String str = "<!DOCTYPE html>" +
                    "        <html>" +
                    "            <head>" +
                    "                <title>“寻艺”APP用户协议</title>" +
                    "                <meta charset=\"utf-8\" />" +
                    "                <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
                    "                <meta name=\"bear-note-unique-identifier\" content=\"CC5CED9C-D9BD-4277-AD3A-1128DC907050-67921-000006C603F98561\">" +
                    "                <meta name=\"created\" content=\"2021-01-05T13:11:59+0800\"/>" +
                    "                <meta name=\"modified\" content=\"2021-01-05T13:21:56+0800\"/>" +
                    "                <meta name=\"tags\" content=\"\"/>" +
                    "                <meta name=\"last device\" content=\"Rorschach\"/>" +
                    "            </head>" +
                    "            <body>" +
                    "                <div class=\"note-wrapper\">" +
                    "                    <h1 id=\"“寻艺”APP用户协议\">“寻艺”APP用户协议</h1>" +
                    "        <p>请您在使用本产品之前，请务必仔细阅读并理解《用户许可使用协议》（以下简称“本协议”）中规定的多项权利和限制。</p>" +
                    "        <br>" +
                    "        <p>本协议是用户（包括通过各种合法途径获取到本产品的自然人、法人或其他组织机构，以下简称“用户”或“您”）与本公司（上海纬岭文化传播有限公司）就注册及使用“寻艺”APP各项服务等相关事宜所订立的协议。</p>" +
                    "        <br>" +
                    "        <p>我们一向尊重并会严格保护用户在使用本产品时的合法权益（包括用户隐私、用户数据等）不受到任何侵犯。</p>" +
                    "        <br>" +
                    "        <p>本协议将对用户使用本产品的行为产生法律约束力，您已承诺和保证有权利和能力订立本协议。用户开始使用本产品将视为已经接受本协议，请认真阅读并理解本协议中各种条款，包括免除和限制我们的免责条款和对用户的权利限制（未成年人审阅时应由法定监护人陪同），如果您不能接受本协议中的全部条款，请勿开始使用本产品。</p>" +
                    "        <br>" +
                    "        <br>" +
                    "        <h2 id=\"使用账户\">使用账户</h2>" +
                    "        <p>您必须承诺和保证：</p>" +
                    "        <ol start=\"1\"><li>您使用本产品的行为必须合法，本产品将会依据本协议“修改和终止”的规定保留或终止您的账户。" +
                    "        </li><li>您必须承诺对您的登录信息保密、不被其他人获取与使用，并且对您在本账户下的所有行为负责。" +
                    "        </li><li>您必须将任何有可能触犯法律的、未授权使用或怀疑为未授权使用的行为在第一时间通知本产品。本产品不对您因未能遵守上述要求而造成的损失承担法律责任。" +
                    "        </li></ol>" +
                    "        <br>" +
                    "        <br>" +
                    "        <h2 id=\"终端用户协议许可\">终端用户协议许可</h2>" +
                    "        <p>依据本协议规定，本产品将授予您以下不可转让的、非排他的许可：</p>" +
                    "        <ol start=\"1\"><li>使用本产品的权利；" +
                    "        </li><li>在您所有的网络通信设备、计算机设备和移动通信设备上下载、安装、使用本产品的权利。" +
                    "        </li></ol>" +
                    "        <br>" +
                    "        <br>" +
                    "        <h2 id=\"限制性条款\">限制性条款</h2>" +
                    "        <p>本协议对您的授权将受到以下限制：</p>" +
                    "        <ol start=\"1\"><li>您不得对本产品进行任何形式的许可、出售、租赁、转让、发行或其他商业用途；" +
                    "        </li><li>除非法律禁止此类限制，否则您不得对本产品的任何部分或衍生产品进行修改、翻译、改编、合并、利用、分解、改造或反向编译、反向工程等；" +
                    "        </li><li>除非法律明文规定，否则您不得对本产品的任何部分以任何形式或方法进行生产、复制、发行、出售、下载或显示等；" +
                    "        </li><li>您不得删除或破坏包含在本产品中的任何版权声明或其他所有权标记。" +
                    "        </li></ol>" +
                    "        <br>" +
                    "        <br>" +
                    "        <h2 id=\"费用\">费用</h2>" +
                    "        <p>您必须自行负担购买本产品的费用，个人上网或第三方（包括但不限于电信或移动通讯提供商）收取的通讯费、信息费等相关费用。如涉及电信增值服务，我们建议您与增值服务提供商确认相关费用问题。</p>" +
                    "        <br>" +
                    "        <br>" +
                    "        <h2 id=\"版本\">版本</h2>" +
                    "        <p>任何本产品的更新版本或未来版本、更新或者其他变更将受到本协议约束。</p>" +
                    "        <br>" +
                    "        <br>" +
                    "        <h2 id=\"遵守法律\">遵守法律</h2>" +
                    "        <p>您同意遵守《中华人民共和国合同法》、《中华人民共和国著作权法》及其实施条例、《全国人民代表大会常务委员会关于维护互联网安全的决定》（“人大安全决定”）、《中华人民共和国保守国家秘密法》、《中华人民共和国电信条例》（“电信条例“）、《中华人民共和国计算机信息系统安全保护条例》、《中华人民共和国计算机信息网络国际联网管理暂行规定》及其实施办法、《计算机信息系统国际联网保密管理规定》、《互联网信息服务管理办法》、《计算机信息网络国际联网安全保护管理办法》、《互联网电子公告服务管理规定》（“电子公告规定”）等相关中国法律法规的任何及所有的规定，并对以任何方式使用您的密码和您的账号使用本服务的任何行为及其结果承担全部责任。如违反《人大安全决定》有可能构成犯罪，被追究刑事责任。《电子公告规定》则有明文规定，上网用户使用电子公告服务系统对所发布的信息负责。《电信条例》也强调，使用电信网络传输信息的内容及其后果由电信用户负责。在任何情况下，如果本网站有理由认为您的任何行为，包括但不限于您的任何言论和其它行为违反或可能违反上述法律和法规的任何规定，本网站可在任何时候不经任何事先通知终止向您提供服务。</p>" +
                    "        <br>" +
                    "        <br>" +
                    "        <h2 id=\"用户内容\">用户内容</h2>" +
                    "        <ol start=\"1\"><li>用户内容是指该用户下载、发布或以其他方式使用本产品时产生的所有内容（例如：您的信息、图片或其他内容）。" +
                    "        </li><li>您是您的用户内容唯一的责任人，您将承担因您的用户内容披露而导致的您或任何第三方被识别的风险。" +
                    "        </li><li>您已同意您的用户内容受到权利限制（详见“权利限制”）" +
                    "        </li></ol>" +
                    "        <br>" +
                    "        <br>" +
                    "        <h2 id=\"权利限制\">权利限制</h2>" +
                    "        <p>您已同意通过分享或其他方式使用本产品中的相关服务，在使用过程中，您将承担因下述行为所造成的风险而产生的全部法律责任：</p>" +
                    "        <ol start=\"1\"><li>破坏宪法所确定的基本原则的；" +
                    "        </li><li>危害国家安全、泄露国家秘密、颠覆国家政权、破坏国家统一的；" +
                    "        </li><li>损害国家荣誉和利益的；" +
                    "        </li><li>煽动民族仇恨、民族歧视，破坏民族团结的；" +
                    "        </li><li>破坏国家宗教政策，宣扬邪教和封建迷信的；" +
                    "        </li><li>散布淫秽、色情、赌博、暴力、凶杀、恐怖或者教唆犯罪的；" +
                    "        </li><li>侮辱或者诽谤他人，侵害他人合法权益的；" +
                    "        </li><li>含有法律、行政法规禁止的其他内容的。" +
                    "        </li></ol>" +
                    "        <br>" +
                    "        <p>您已经同意不在本产品从事下列行为：</p>" +
                    "        <ol start=\"1\"><li>发布或分享电脑病毒、蠕虫、恶意代码、故意破坏或改变计算机系统或数据的软件；" +
                    "        </li><li>未授权的情况下，收集其他用户的信息或数据，例如电子邮箱地址等；" +
                    "        </li><li>用自动化的方式恶意使用本产品，给服务器造成过度的负担或以其他方式干扰或损害网站服务器和网络链接；" +
                    "        </li><li>在未授权的情况下，尝试访问本产品的服务器数据或通信数据；" +
                    "        </li><li>干扰、破坏本产品其他用户的使用。" +
                    "        </li></ol>" +
                    "        <br>" +
                    "        <br>" +
                    "        <h2 id=\"修改和终止\">修改和终止</h2>" +
                    "        <h3 id=\"修改\">修改</h3>" +
                    "        <p>本协议容许变更。如果本协议有任何实质性变更，我们将通过电子邮件来通知您。变更通知之后，继续使用本产品则为您已知晓此类变更并同意条款约束；</p>" +
                    "        <p>我们保留在任何时候无需通知而修改、保留或关闭本产品任何服务之权利；</p>" +
                    "        <p>您已同意我们无需因修改、保留或关闭本产品任何服务之权利；</p>" +
                    "        <p>您已同意我们无需因修改、保留或关闭本产品任何服务的行为对您或第三方承担责任。</p>" +
                    "        <br>" +
                    "        <h3 id=\"终止\">终止</h3>" +
                    "        <p>本协议自您接受之日起生效，在您使用本产品的过程中持续有效，直至依据本协议终止；</p>" +
                    "        <p>尽管有上述规定，如果您使用本产品的时间早于您接受本协议的时间，您在此知晓并同意本协议于您接受本协议的时间，您在此知晓并同意本协议于您第一次使用本产品时生效，除非依据本协议提前终止；</p>" +
                    "        <p>我们可能会依据法律的规定，保留您使用本产品或者本账户的权利；无论是否通知，我们将在任何时间以任何原因终止本协议，包括出于善意的相信您违反了我们可接受使用政策或本协议的其他规定；</p>" +
                    "        <p>不受前款规定所限，如果用户侵犯第三人的版权且我们接到版权所有人或版权所有人的合法代理人的通知后，我们保留终止本协议的权利；</p>" +
                    "        <p>一旦本协议终止，您使用本产品的权利即告终止。您应当知晓您的产品终止意味着您的用户内容将从我们的活动数据库中删除。我们不因终止本协议对您承担任何责任，包括终止您的用户账户和删除您的用户内容。</p>" +
                    "        <br>" +
                    "        <br>" +
                    "        <h2 id=\"适用法律\">适用法律</h2>" +
                    "        <p>本协议适用中华人民共和国法律；</p>" +
                    "        <p>如果双方发生纠纷，应本着友好的圆柱协商解决；如协商不成，应向所在地的法院提起诉讼。</p>" +
                    "                </div>" +
                    "                <script type=\"text/javascript\">" +
                    "                    (function() {" +
                    "" +
                    "            var doc_ols = document.getElementsByTagName(\"ol\");" +
                    "" +
                    "            for ( i=0; i<doc_ols.length; i++) {" +
                    "" +
                    "                var ol_start = doc_ols[i].getAttribute(\"start\") - 1;" +
                    "                doc_ols[i].setAttribute(\"style\", \"counter-reset:ol_counter \" + ol_start + \";\");" +
                    "" +
                    "            }" +
                    "" +
                    "        })();" +
                    "" +
                    "                </script>" +
                    "                <style>" +
                    "                    html,body,div,span,applet,object,iframe,h1,h2,h3,h4,h5,h6,p,blockquote,pre,a,abbr,acronym,address,big,cite,code,del,dfn,em,img,ins,kbd,q,s,samp,small,strike,strong,sub,sup,tt,var,b,u,i,center,dl,dt,dd,ol,ul,li,fieldset,form,label,legend,table,caption,tbody,tfoot,thead,tr,th,td,article,aside,canvas,details,embed,figure,figcaption,footer,header,hgroup,menu,nav,output,ruby,section,summary,time,mark,audio,video{margin:0;padding:0;border:0;font:inherit;font-size:100%;vertical-align:baseline}html{line-height:1}ol,ul{list-style:none}table{border-collapse:collapse;border-spacing:0}caption,th,td{text-align:left;font-weight:normal;vertical-align:middle}q,blockquote{quotes:none}q:before,q:after,blockquote:before,blockquote:after{content:\"\";content:none}a img{border:none}article,aside,details,figcaption,figure,footer,header,hgroup,main,menu,nav,section,summary{display:block}*{-moz-box-sizing:border-box;-webkit-box-sizing:border-box;box-sizing:border-box}html{font-size:87.5%;line-height:1.57143em}html{font-size:14px;line-height:1.6em;-webkit-text-size-adjust:100%}body{background:#fcfcfc;color:#545454;text-rendering:optimizeLegibility;font-family:\"AvenirNext-Regular\"}a{color:#de4c4f;text-decoration:none}h1{font-family:\"AvenirNext-Medium\";color:#333;font-size:1.6em;line-height:1.3em;margin-bottom:.78571em}h2{font-family:\"AvenirNext-Medium\";color:#333;font-size:1.3em;line-height:1em;margin-bottom:.62857em}h3{font-family:\"AvenirNext-Medium\";color:#333;font-size:1.15em;line-height:1em;margin-bottom:.47143em}p{margin-bottom:1.57143em;hyphens:auto}hr{height:1px;border:0;background-color:#dedede;margin:-1px auto 1.57143em auto}ul,ol{margin-bottom:.31429em}ul ul,ul ol,ol ul,ol ol{margin-bottom:0px}ol{counter-reset:ol_counter}ol li:before{content:counter(ol_counter) \".\";counter-increment:ol_counter;color:#e06e73;text-align:right;display:inline-block;min-width:1em;margin-right:0.5em}b,strong{font-family:\"AvenirNext-Bold\"}i,em{font-family:\"AvenirNext-Italic\"}code{font-family:\"Menlo-Regular\"}.text-overflow-ellipsis{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.sf_code_string,.sf_code_selector,.sf_code_attr-name,.sf_code_char,.sf_code_builtin,.sf_code_inserted{color:#D33905}.sf_code_comment,.sf_code_prolog,.sf_code_doctype,.sf_code_cdata{color:#838383}.sf_code_number,.sf_code_boolean{color:#0E73A2}.sf_code_keyword,.sf_code_atrule,.sf_code_rule,.sf_code_attr-value,.sf_code_function,.sf_code_class-name,.sf_code_class,.sf_code_regex,.sf_code_important,.sf_code_variable,.sf_code_interpolation{color:#0E73A2}.sf_code_property,.sf_code_tag,.sf_code_constant,.sf_code_symbol,.sf_code_deleted{color:#1B00CE}.sf_code_macro,.sf_code_entity,.sf_code_operator,.sf_code_url{color:#920448}.note-wrapper{max-width:46em;margin:0px auto;padding:1.57143em 3.14286em}.note-wrapper.spotlight-preview{overflow-x:hidden}u{text-decoration:none;background-image:linear-gradient(to bottom, rgba(0,0,0,0) 50%,#e06e73 50%);background-repeat:repeat-x;background-size:2px 2px;background-position:0 1.05em}s{color:#878787}p{margin-bottom:0.1em}hr{margin-bottom:0.7em;margin-top:0.7em}ul li{text-indent:-0.35em}ul li:before{content:\"•\";color:#e06e73;display:inline-block;margin-right:0.3em}ul ul{margin-left:1.25714em}ol li{text-indent:-1.45em}ol ol{margin-left:1.25714em}blockquote{display:block;margin-left:-1em;padding-left:0.8em;border-left:0.2em solid #e06e73}.todo-list ul{margin-left:1.88571em}.todo-list li{text-indent:-1.75em}.todo-list li:before{content:\"\";display:static;margin-right:0px}.todo-checkbox{text-indent:-1.7em}.todo-checkbox svg{margin-right:0.3em;position:relative;top:0.2em}.todo-checkbox svg #check{display:none}.todo-checkbox.todo-checked #check{display:inline}.todo-checkbox.todo-checked+.todo-text{text-decoration:line-through;color:#878787}.code-inline{display:inline;background:white;border:solid 1px #dedede;padding:0.2em 0.5em;font-size:0.9em}.code-multiline{display:block;background:white;border:solid 1px #dedede;padding:0.7em 1em;font-size:0.9em;overflow-x:auto}.hashtag{display:inline-block;color:white;background:#b8bfc2;padding:0.0em 0.5em;border-radius:1em;text-indent:0}.hashtag a{color:#fff}.address a{color:#545454;background-image:linear-gradient(to bottom, rgba(0,0,0,0) 50%,#0da35e 50%);background-repeat:repeat-x;background-size:2px 2px;background-position:0 1.05em}.address svg{position:relative;top:0.2em;display:inline-block;margin-right:0.2em}.color-preview{display:inline-block;width:1em;height:1em;border:solid 1px rgba(0,0,0,0.3);border-radius:50%;margin-right:0.1em;position:relative;top:0.2em;white-space:nowrap}.color-code{margin-right:0.2em;font-family:\"Menlo-Regular\";font-size:0.9em}.color-hash{opacity:0.4}.ordered-list-number{color:#e06e73;text-align:right;display:inline-block;min-width:1em}.arrow svg{position:relative;top:0.08em;display:inline-block;margin-right:0.15em;margin-left:0.15em}.arrow svg #rod{stroke:#545454}.arrow svg #point{fill:#545454}mark{color:inherit;display:inline;padding:0.2em 0.5em;background-color:#fcffc0}img{max-width:100%;height:auto}" +
                    "" +
                    "                </style>" +
                    "            </body>" +
                    "        </html>";
            return R.OK(str);
        } else {
            return R.ERROR("type:[1,2]");
        }
    }

    public R<ResAppVersion> appVersionCheck(Integer version_code) {
        LambdaQueryWrapper<XunyeeAppVersion> qw = new LambdaQueryWrapper<>();
        qw.gt(version_code != null, XunyeeAppVersion::getVersion_code, version_code)
                .orderByDesc(XunyeeAppVersion::getVersion_code)
                .last("limit 1");
        XunyeeAppVersion appVersion = new XunyeeAppVersion().selectOne(qw);
        if (appVersion == null) {
            return R.ERROR("您当前已是最新版本");
        }
        appVersion.setApk_download_url(imageHostUtil.absImagePath(appVersion.getApk_download_url()));
        ResAppVersion resAppVersion = BeanUtil.copyProperties(appVersion, ResAppVersion.class);
        return R.OK(resAppVersion);
    }


    public R brandBrow(int userId, int brand_id) {
        // 添加一条浏览记录
        LambdaQueryWrapper<XunyeeBrandBrowsingHistory> hqw = new LambdaQueryWrapper();
        hqw.eq(XunyeeBrandBrowsingHistory::getBrand_id, brand_id)
                .eq(XunyeeBrandBrowsingHistory::getVcuser_id, userId);
        XunyeeBrandBrowsingHistory temp = new XunyeeBrandBrowsingHistory().selectOne(hqw);
        if (temp == null) {
            XunyeeBrandBrowsingHistory history = new XunyeeBrandBrowsingHistory();
            history.setVcuser_id(userId);
            history.setBrand_id(brand_id);
            if (history.insert()) {
                return R.OK();
            }

        } else {
            temp.setLast_brow_time(new Date());
            temp.updateById();
            if (temp.updateById()) {
                return R.OK();
            }
        }
        return R.ERROR();
    }

    public R<List<ResBrandPersonList>> brandBrowHistory(int userId) {

        LambdaQueryWrapper<XunyeeBrandBrowsingHistory> qw = new LambdaQueryWrapper();
        qw.select(XunyeeBrandBrowsingHistory::getBrand_id)
                .eq(XunyeeBrandBrowsingHistory::getVcuser_id, userId)
                .orderByDesc(XunyeeBrandBrowsingHistory::getCreated)
                .last("limit 9");
        List<XunyeeBrandBrowsingHistory> history = new XunyeeBrandBrowsingHistory().selectList(qw);
        List<ResBrandPersonList> resBrandPeople = new ArrayList<>();
        if (history.size() > 0) {
            // 提取teleplay id去数据库查询电视剧信息
            List<Integer> ids = history.stream().map(e -> e.getBrand_id()).collect(Collectors.toList());
            List<ResBrandPersonList> list = metaService.getBrandByIds(ids);
            return R.OK(list);
        }

        return R.OK(resBrandPeople);
    }

    public R brandStarRate() {
        List<SdbJdSale> sdbJdSales = starService.getWeekJDSaleRank();
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

    public R brandStarRank(String name) {

        List<SdbJdSale> sdbJdSales = starService.getWeekJDSaleRank();
        if (sdbJdSales.size() <= 0) {
            return R.ERROR("数据查询失败");
        }

        // 提取person id去数据库查询电视剧信息
        Integer[] personIds = sdbJdSales.stream().map(e -> e.getPerson_id()).collect(Collectors.toList())
                .toArray(new Integer[sdbJdSales.size()]);

        // 查询数据库艺人信息
        List<Person> persons = metaService.getPerson(personIds);

        // 查询艺人代言的品牌
        List<ResBrandPersonList> brandPersonLists = metaService.getPersonBrandListByPersonIds(personIds);

        ResSdbJdSaleRank resSdbJdSaleRank = new ResSdbJdSaleRank();
        LocalDate lastDate = sdbJdSales.get(0).getCreated();
        resSdbJdSaleRank.setStart_date(lastDate.minusDays(6));
        resSdbJdSaleRank.setFinish_date(lastDate);

        for (SdbJdSale sdbJdSale : sdbJdSales) {
            ResSdbJdSaleRank.Rank rank = new ResSdbJdSaleRank.Rank();
            for (Person person : persons) {
                //这里使用equals 比较 Integer
                if (sdbJdSale.getPerson_id().equals(person.getId())) {
                    rank.setPerson(person.getId());
                    rank.setZh_name(person.getZh_name());
                    rank.setAvatar(imageHostUtil.absImagePath(person.getAvatar_custom()));
                    break;
                }
            }
        }

        return R.OK();

    }
}
