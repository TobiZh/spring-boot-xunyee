package com.vlinkage.xunyee.api.signin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mongodb.client.result.UpdateResult;
import com.vlinkage.ant.meta.entity.Person;
import com.vlinkage.ant.xunyee.entity.XunyeePic;
import com.vlinkage.ant.xunyee.entity.XunyeeVcuserBenefit;
import com.vlinkage.xunyee.api.provide.MetaService;
import com.vlinkage.xunyee.entity.request.ReqMonUserPersonCheck;
import com.vlinkage.xunyee.entity.response.ResMonUserPersonCheck;
import com.vlinkage.xunyee.entity.response.ResPicTitleUrl;
import com.vlinkage.xunyee.entity.result.R;
import com.vlinkage.xunyee.entity.result.code.ResultCode;
import com.vlinkage.xunyee.utils.ImageHostUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SigninService {

    @Autowired
    private MetaService metaService;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ImageHostUtil imageHostUtil;

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
                .ge(XunyeeVcuserBenefit::getFinish_time, gteDate)
                .orderByDesc(XunyeeVcuserBenefit::getFinish_time)
                .last("limit 1");
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
                .ge(XunyeeVcuserBenefit::getFinish_time, gteDate)
                .orderByDesc(XunyeeVcuserBenefit::getFinish_time)
                .last("limit 1");
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
}
