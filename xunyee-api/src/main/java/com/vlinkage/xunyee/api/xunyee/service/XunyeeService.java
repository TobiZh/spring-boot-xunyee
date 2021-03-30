package com.vlinkage.xunyee.api.xunyee.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mongodb.client.result.UpdateResult;
import com.vlinkage.ant.meta.entity.Person;
import com.vlinkage.ant.star.entity.PersonGallery;
import com.vlinkage.ant.xunyee.entity.*;
import com.vlinkage.ant.xunyee.mapper.XunyeeSystemNotificationMapper;
import com.vlinkage.ant.xunyee.mapper.XunyeeVcuserBenefitMapper;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.meta.service.MetaService;
import com.vlinkage.xunyee.api.pay.service.PayService;
import com.vlinkage.xunyee.api.star.service.StarService;
import com.vlinkage.xunyee.entity.ReqMyPage;
import com.vlinkage.xunyee.entity.request.*;
import com.vlinkage.xunyee.entity.response.*;
import com.vlinkage.xunyee.mapper.MyMapper;
import com.vlinkage.xunyee.utils.CopyListUtil;
import com.vlinkage.xunyee.utils.DateUtil;
import com.vlinkage.xunyee.utils.OrderCodeFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class XunyeeService {

    @Value("${sys-config.image-path}")
    private String imagePath;

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
    private XunyeeSystemNotificationMapper notificationMapper;

    @Resource
    private XunyeeVcuserBenefitMapper xunyeeVcuserBenefitMapper;


    public R<Map> getPic(ReqPic req) {
        LocalDateTime nowDate = LocalDateTime.now();

        QueryWrapper qw = new QueryWrapper();
        qw.eq("type_id", req.getType());
        if (req.getIs_enabled_5() != null) {
            qw.eq("is_enabled_5", req.getIs_enabled_5() == 0 ? false : true);
        }
        if (req.getIs_enabled_6() != null) {
            qw.eq("is_enabled_6", req.getIs_enabled_6() == 0 ? false : true);
        }
        qw.le("start_time", nowDate);// >=
        qw.ge("finish_time", nowDate);// <=
        qw.orderByAsc("sequence");
        List<XunyeePic> xunyeePics = new XunyeePic().selectList(qw);
        List<ResPic> resPics = CopyListUtil.copyListProperties(xunyeePics, ResPic.class);
        for (ResPic p : resPics) {
            p.setPic(imagePath + p.getPic());
        }
        Map map = new HashMap();
        map.put("count", resPics.size());
        map.put("results", resPics);
        return R.OK(map);
    }

    public R<List<ResNavigation>> getNavigation() {
        QueryWrapper qw = new QueryWrapper();
        qw.orderByAsc("orderby");
        qw.eq("is_deleted", 0);
        qw.eq("is_enabled", 1);
        List<XunyeeNavigation> navigation = new XunyeeNavigation().selectList(qw);
        List<ResNavigation> resNavigations = CopyListUtil.copyListProperties(navigation, ResNavigation.class);
        return R.OK(resNavigations);
    }

    public R<List<ResSearchHot>> getSearchHot() {
        QueryWrapper qw = new QueryWrapper();
        qw.eq("is_deleted", false);
        qw.orderByAsc("orderby");
        qw.select("id", "name");
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

        QueryWrapper qw = new QueryWrapper();
        qw.eq("receive_vcuser_id", userId);
        qw.or();
        qw.eq("receive_vcuser_id",0);
        qw.orderByDesc("created");
        Page page = new Page(myPage.getCurrent(), myPage.getSize());
        IPage<ResSystemNotification> iPage = new XunyeeSystemNotification().selectPage(page, qw);
        iPage.setRecords(CopyListUtil.copyListProperties(iPage.getRecords(), ResSystemNotification.class));

        return R.OK(iPage);
    }

    public R systemNotificationRead(int userId, int id) {
        XunyeeSystemNotification notification = new XunyeeSystemNotification().selectById(id);
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
        String sql="update xunyee_system_notification set is_read=1,read_time=CURRENT_TIMESTAMP " +
                "where receive_vcuser_id="+userId+" and is_read=0";
        int count=jdbcTemplate.update(sql);
        if (count<0){
            return R.ERROR();
        }
        return R.OK();
    }

    public R vcuserBenefit(int userId) {
        QueryWrapper qw = new QueryWrapper();
        qw.select("start_time", "finish_time");
        qw.eq("vcuser_id", userId);
        XunyeeVcuserBenefit benefit = new XunyeeVcuserBenefit().selectOne(qw);
        ResBenefit resBenefit = BeanUtil.copyProperties(benefit, ResBenefit.class);
        return R.OK(resBenefit);
    }

    public R<ResRank<ResPersonCheckCount>> personCheckCount(Integer userId, ReqPersonCheckCount req) {

        int period = req.getPeriod();
        int current = req.getCurrent();
        int size = req.getSize();
        int rankStart = 1; // 分页rank起始值

        LocalDate nowDate = LocalDate.now();//今天
        LocalDate gteDate; // >=
        LocalDate ltDate; // <
        if (period <= 1) {//获取今天签到榜
            gteDate = nowDate;
            ltDate = gteDate.plusDays(1); // <
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
        if (resMgs.size() <= 0) {
            //todo 这里做一个读取缓存
        }


        // 查询所有的可签到艺人 大概500个
        List<Person> persons = metaService.getPersonByXunyeeCheck();
        // 查询记录总数 数据总页数
        int totalCount = persons.size();
        int totalPage = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;
        // 查询当前用户关注的艺人和当天签到数
        List<ResMonUserPersonCheck> userPersonChecks = period <= 1 && userId != null ? mongoTemplate.find(new Query(Criteria.where("vcuser").is(userId).and("updated").gte(gteDate).lt(ltDate)),
                ResMonUserPersonCheck.class) : new ArrayList<>();

        // 组装数据
        List<ResPersonCheckCount> resCheckCounts = new ArrayList<>();
        for (Person p : persons) {
            int personId = p.getId();
            ResPersonCheckCount resPersonCheckCount = new ResPersonCheckCount();
            resPersonCheckCount.setPerson(personId);
            resPersonCheckCount.setId(personId);
            resPersonCheckCount.setVcuser_person("");
            resPersonCheckCount.setAvatar_custom(p.getAvatar_custom());
            resPersonCheckCount.setZh_name(p.getZh_name());
            for (int i = 0; i < resMgs.size(); i++) {
                ResMonPersonCheckCount mon = resMgs.get(i);
                Integer tmpPerson = mon.getId();
                //-------------------- 当前用户>>艺人签到数 --------------------
                if (period <= 1) {
                    for (int j = 0; j < userPersonChecks.size(); j++) {
                        if (personId == userPersonChecks.get(j).getPerson()) {
                            resPersonCheckCount.setCheck_my(userPersonChecks.get(j).getCheck());
                            break;
                        }
                    }
                }
                //-------------------- 当前用户>>艺人签到数 --------------------

                //-------------------- 当前艺人头签到数 --------------------
                if (personId == tmpPerson) {
                    resPersonCheckCount.setCheck(mon.getCheck());
                    break;
                }
                //-------------------- 当前艺人头签到数 --------------------
            }
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


//        // 查询当前用户关注的艺人和今年签到的天数
//        List<ResMonUserPersonCheck> resMonUserPersonChecks = mongoTemplate.find(new Query(Criteria.where("vcuser").is(userId).and("updated").gte(gteDate).lt(ltDate)), ResMonUserPersonCheck.class);


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

    public R<ResXunyeeBenefitPrice> benefitPrice() {
        LocalDateTime nowDate = LocalDateTime.now();
        QueryWrapper qw = new QueryWrapper();
        qw.eq("is_enabled", true);
        qw.le("start_time", nowDate);// >=
        qw.ge("finish_time", nowDate);// <=
        qw.orderByAsc("quantity");
        List<XunyeeBenefitPrice> benefitPrices = new XunyeeBenefitPrice().selectList(qw);
        List<ResXunyeeBenefitPrice> resXunyeeBenefitPrices = CopyListUtil.copyListProperties(benefitPrices, ResXunyeeBenefitPrice.class);
        return R.OK(resXunyeeBenefitPrices);
    }


    /**
     * 会员一天 3次只能签一个人
     * 非会员一天 3人1次
     *
     * @param userId
     * @param req
     * @return
     */
    public R vcuserPersonCheck(int userId, ReqPersonCheck req) {
        int personId = req.getPerson();
        Person person = metaService.getPersonById(personId);
        if (!person.getIs_xunyee_check()) {
            return R.ERROR("该艺人已关闭签到");
        }

        LocalDate gteDate = LocalDate.now(); // >=
        LocalDate ltDate = gteDate.plusDays(1); // <; // <

        QueryWrapper qw = new QueryWrapper();
        qw.eq("vcuser_id", userId);
        qw.ge("start_time", gteDate);//<=
        qw.lt("finish_time", ltDate);// >
        XunyeeVcuserBenefit vcuserBenefit = new XunyeeVcuserBenefit().selectOne(qw);

        // 查询当前用户关注的艺人和当天签到数
        List<ResMonUserPersonCheck> resPersonChecks = mongoTemplate.find(new Query(Criteria.where("vcuser")
                        .is(userId).and("updated")
                        .gte(gteDate).lt(ltDate)),
                ResMonUserPersonCheck.class);
        // 今日已使用次数
        int checkCounted = resPersonChecks.stream().collect(Collectors.summingInt(ResMonUserPersonCheck::getCheck));
        if (checkCounted >= 3) {
            return R.ERROR("每天对所有艺人的签到数不能超过3。");
        }
        // 是否已为该艺人签到过
        boolean b = resPersonChecks.stream().anyMatch(task -> task.getPerson().equals(personId));
        if (b) {
            return R.ERROR("今天已经签到过了，明天再来吧");
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

    public R<ResPersonInfo> vcuserPersonPersonInfo(Integer userId, int person) {
        Person resPerson = metaService.getPersonById(person);
        if (resPerson == null) {
            return R.ERROR("艺人id错误");
        }
        LocalDate gteDate = LocalDate.now(); // >=
        LocalDate ltDate = gteDate.plusDays(1); // <; // <

        // 当前艺人指数
        ResMonReportPersonRptTrend resMongo = mongoTemplate.findOne(Query.query(Criteria.where("person").is(person)), ResMonReportPersonRptTrend.class);

        ResPersonInfo info = new ResPersonInfo();
        info.setZh_name(resPerson.getZh_name());
        info.setPerson(resPerson.getId());
        info.setAvatar_custom(imagePath + resPerson.getAvatar_custom());
        info.setSex(resPerson.getSex() == 1 ? "男" : "女");
        if (resMongo!=null){
            info.setReport_1912_teleplay(resMongo.getReport_1912_teleplay());
            info.setReport_1912_teleplay_rank(resMongo.getReport_1912_teleplay_rank());
            info.setReport_1912_teleplay_rank_incr(resMongo.getReport_1912_teleplay_rank_incr());
        }else{
            info.setReport_1912_teleplay(5);
        }

        // 当前艺人今日签到数
        ResMonPersonCheckCount personCheckCount = mongoTemplate.findOne(Query.query(Criteria.where("person").is(person).andOperator(Criteria.where("data_time").gte(gteDate).lt(ltDate))), ResMonPersonCheckCount.class);
        if (personCheckCount != null) {
            info.setCkeck(person);
        }
        // 查询当前用户关注的艺人和当天签到数
        if (userId != null) {
            ResMonUserPersonCheck userPersonCheck = mongoTemplate.findOne(Query.query(Criteria.where("vcuser").is(userId)
                            .and("person").is(person)
                            .andOperator(Criteria.where("updated").gte(gteDate).lt(ltDate))),
                    ResMonUserPersonCheck.class);
            if (userPersonCheck != null) {
                info.setCheck_my(userPersonCheck.getCheck());
            }
        }

        return R.OK(info);
    }

    public R<List<ResReportPersonRptTrendQux>> reportPersonRptTrendAll(int person) {
        // 当前艺人指数
        LocalDate gteDate = LocalDate.now().minusDays(10); // >=
        LocalDate ltDate = LocalDate.now(); // <; // <
        // count的查询条件
        Criteria criteria = Criteria.where("period").is(1).and("person").in(person)
                .andOperator(Criteria.where("updated").gte(gteDate).lt(ltDate));
        Query query = new Query();
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "updated"));
        List<ResMonReportPersonRptTrend> resMongo = mongoTemplate.find(query, ResMonReportPersonRptTrend.class);

        return R.OK(resMongo);
    }

    public R reportPersonAlbum(ReqMyPage myPage, int person) {
        IPage iPage = starService.getPersonGalleryByPersonId(person, myPage);
        List<PersonGallery> resBlogPages = iPage.getRecords();
        List<String> list = resBlogPages.stream().map(c -> imagePath + c.getOriginal()).collect(Collectors.toList());
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
        QueryWrapper bqw = new QueryWrapper();
        bqw.eq("vcuser_id", userId);
        bqw.ge("finish_time", nowDate);// <=
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
            vcuserBenefit.setFinish_time(nowDate);
            vcuserBenefit.setFinish_time(nowDate.plusDays(plusDays));
            if (!vcuserBenefit.insert()) {
                return R.ERROR();
            }
        } else {

            // 这里使用xunyeeVcuserBenefitMapper 是因为 pgsql的主键使用的是uuid类型，不能updateById;
            QueryWrapper updateQw = new QueryWrapper();
            updateQw.eq("id", UUID.fromString(temp.getId()));
            temp.setBenefit_id(benefitId);
            temp.setUpdated(date);
            temp.setFinish_time(nowDate.plusDays(plusDays));
            if (xunyeeVcuserBenefitMapper.update(temp, updateQw) <= 0) {
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

    public R vcuserBenefitPayOrderSubmit(HttpServletRequest request, int userId, ReqBenefitPayOrder req) {
        int site = req.getSite();

        XunyeeBenefitPrice price = new XunyeeBenefitPrice().selectById(req.getBenefit_price());
        if (price == null) {
            return R.ERROR("您购买的会员服务不存在");
        }

        long orderNo = Long.parseLong(OrderCodeFactory.getSimpleOrderCode((long) userId));
        XunyeeVcuserBenefitPayorder payorder = new XunyeeVcuserBenefitPayorder();
        payorder.setVcuser_id(userId);
        payorder.setBenefit_price_id(price.getId());
        payorder.setIs_paid(false);
        payorder.setQuantity(price.getQuantity());
        payorder.setSite(site);
        payorder.setPrice(price.getPrice());
        payorder.setRel_order_id(orderNo);

        Date nowDate = new Date();
        payorder.setUpdated(nowDate);
        payorder.setCreated(nowDate);
        if (payorder.insert()) {
            return R.OK(payService.payBenefit(request,payorder));
        }
        return R.ERROR("下单失败");
    }

    public R<Map<String,Object>> globalSearch(Integer userId,ReqMyPage myPage,ReqGlobalSearch reqGlobalSearch) {

        String keyword=reqGlobalSearch.getName();
        List<ResPerson> persons=metaService.getPersonLimit(keyword,3);
        Page page=new Page(myPage.getCurrent(),myPage.getSize());
        IPage<ResBlogPage> iPage=myMapper.selectBlogBySearch(page,keyword,userId);
        for (ResBlogPage record : iPage.getRecords()) {
            if (StringUtils.isNotEmpty(record.getCover())){
                record.setCover(imagePath+record.getCover());
            }
        }

        Map<String,Object> map=new HashMap<>();
        map.put("persons",persons);
        map.put("blog_page",iPage);
        return R.OK(map);
    }
}
