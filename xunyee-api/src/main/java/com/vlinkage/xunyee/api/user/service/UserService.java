package com.vlinkage.xunyee.api.user.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vlinkage.ant.meta.entity.Person;
import com.vlinkage.ant.xunyee.entity.*;
import com.vlinkage.ant.xunyee.mapper.XunyeeVcuserMapper;
import com.vlinkage.xunyee.config.redis.RedisUtil;
import com.vlinkage.xunyee.entity.result.R;
import com.vlinkage.xunyee.api.upload.service.UploadService;
import com.vlinkage.xunyee.api.provide.MetaService;
import com.vlinkage.xunyee.entity.ReqMyPage;
import com.vlinkage.xunyee.entity.request.ReqPageFollow;
import com.vlinkage.xunyee.entity.request.ReqUserInfo;
import com.vlinkage.xunyee.entity.request.ReqUserReport;
import com.vlinkage.xunyee.entity.response.*;
import com.vlinkage.xunyee.mapper.MyMapper;
import com.vlinkage.xunyee.utils.CopyListUtil;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private ImageHostUtil imageHostUtil;

    @Autowired
    private MyMapper myMapper;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MetaService metaService;

    @Autowired
    private UploadService uploadService;

    @Resource
    private XunyeeVcuserMapper vcuserMapper;

    @Autowired
    private RedisUtil redisUtil;

    public R<ResUserInfoOhter> other(Integer mine_vcuser_id, Integer to_vcuser_id) {
        int follow_type = 0;//前端直接显示 0 关注， 1 回关，2 已关注，3 互相关注
        List<String> personList = new ArrayList<>();//共同关注的艺人

        if (mine_vcuser_id != null && mine_vcuser_id != to_vcuser_id) {
            // ===============  我是否以前关注过该用户 follow_type 关注状态 ====================
            LambdaQueryWrapper<XunyeeFollow> foqw = new LambdaQueryWrapper<>();
            foqw.eq(XunyeeFollow::getVcuser_id, mine_vcuser_id)
                    .eq(XunyeeFollow::getFollowed_vcuser_id, to_vcuser_id)
                    .eq(XunyeeFollow::getStatus, 1)
                    .or()
                    .eq(XunyeeFollow::getVcuser_id, to_vcuser_id)
                    .eq(XunyeeFollow::getFollowed_vcuser_id, mine_vcuser_id)
                    .eq(XunyeeFollow::getStatus, 1);
            List<XunyeeFollow> follows = new XunyeeFollow().selectList(foqw);
            if (follows.size() > 0) {
                for (XunyeeFollow follow : follows) {
                    if (follow.getType() == 3) {
                        follow_type = follow.getType();
                    } else {
                        if (follow.getVcuser_id() == to_vcuser_id) {
                            follow_type = follow.getType();
                        } else {
                            follow_type = 2;//回关
                        }
                    }
                }
            }
            // ===============  我是否以前关注过该用户  ====================

            // ===============  共同关注  ====================
            // 查询对方关注的艺人
            Criteria criteria = new Criteria().where("vcuser").is(to_vcuser_id).and("is_enabled").is(true);
            Query query = new Query(criteria);
            List<ResMonUserPerson> resMGs = mongoTemplate.find(query, ResMonUserPerson.class);

            // 查询我关注的艺人
            Criteria criteriaMine = new Criteria().where("vcuser").is(mine_vcuser_id).and("is_enabled").is(true);
            Query queryMine = new Query(criteriaMine);
            List<ResMonUserPerson> resMGsMine = mongoTemplate.find(queryMine, ResMonUserPerson.class);

            // 提取person id去数据库查询电视剧信息并取出并集
            List<Integer> personIdsOther = resMGs.stream().map(e -> e.getPerson()).collect(Collectors.toList());
            List<Integer> personIdsMine = resMGsMine.stream().map(e -> e.getPerson()).collect(Collectors.toList());
            List<Integer> intersection = personIdsOther.stream().filter(item -> personIdsMine.contains(item)).collect(Collectors.toList());

            if (intersection.size() > 0) {
                // 查询数据库艺人信息
                Integer[] personIdArr = intersection.toArray(new Integer[intersection.size()]);

                List<Person> persons = metaService.getPerson(personIdArr);
                for (Person person : persons) {
                    personList.add(person.getZh_name());
                }
            }
            // ===============  共同关注  ====================

        }

        // ===============  是不是会员  ====================
        LocalDate nowDate = LocalDate.now();
        LambdaQueryWrapper<XunyeeVcuserBenefit> qw = new LambdaQueryWrapper();
        qw.eq(XunyeeVcuserBenefit::getVcuser_id, to_vcuser_id)
                .le(XunyeeVcuserBenefit::getStart_time, nowDate)
                .ge(XunyeeVcuserBenefit::getFinish_time, nowDate);
        int benefit = new XunyeeVcuserBenefit().selectCount(qw);
        boolean is_vip = benefit > 0;
        // ===============  是不是会员  ====================

        // ===============  我的关注  ====================
        LambdaQueryWrapper<XunyeeFollow> fqw = new LambdaQueryWrapper<>();
        fqw.eq(XunyeeFollow::getVcuser_id, to_vcuser_id)
                .eq(XunyeeFollow::getStatus, 1);
        int follow_count = new XunyeeFollow().selectCount(fqw);
        // ===============  我的关注  ====================

        // ===============  我的粉丝  ====================
        LambdaQueryWrapper<XunyeeFollow> tqw = new LambdaQueryWrapper<>();
        tqw.eq(XunyeeFollow::getFollowed_vcuser_id, to_vcuser_id)
                .eq(XunyeeFollow::getStatus, 1);
        int fans_count = new XunyeeFollow().selectCount(tqw);
        // ===============  我的粉丝  ====================

        // ===============  我的点赞  ====================
        String sql = "SELECT COALESCE(sum(star_count),0) star_count FROM xunyee_blog where vcuser_id=" + to_vcuser_id;
        int star_count = jdbcTemplate.queryForObject(sql, int.class);
        // ===============  我的点赞  ====================


        // ===============  我的爱豆数量  ====================
        int idol_count = (int) mongoTemplate.count(new Query(Criteria.where("vcuser").is(to_vcuser_id).and("is_enabled").is(true)), ResMonUserPerson.class);
        // ===============  我的爱豆数量  ====================

        // =============== 查询当前用户关注的艺人和今年签到的天数 ===============
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("vcuser").is(to_vcuser_id).and("updated").gte(DateUtil.getCurrYearFirst(LocalDate.now().getYear()))),
                Aggregation.project("merchno", "amount")
                        .andExpression("{ $dateToString:{format:'%Y-%m-%d',date: '$updated',timezone: 'Asia/Shanghai' }}").as("date"),
                Aggregation.group("date").count().as("check")
        );

        AggregationResults<ResMonUserPersonCheck> res = mongoTemplate.aggregate(aggregation, "vc_user__person__check", ResMonUserPersonCheck.class);
        int check_days_count = res.getMappedResults().size();
        // =============== 查询当前用户关注的艺人和今年签到的天数 ===============


        ResUserInfoOhter resMine = new ResUserInfoOhter();

        // =========================== 用户信息 ===========================
        LambdaQueryWrapper<XunyeeVcuser> uqw = new LambdaQueryWrapper<>();
        uqw.select(XunyeeVcuser::getId, XunyeeVcuser::getNickname, XunyeeVcuser::getBio, XunyeeVcuser::getAvatar, XunyeeVcuser::getCover)
                .eq(XunyeeVcuser::getId, to_vcuser_id);
        XunyeeVcuser toVcuser = new XunyeeVcuser().selectOne(uqw);
        resMine.setVcuser_id(toVcuser.getId());
        resMine.setNickname(StringUtils.isNotEmpty(toVcuser.getNickname()) ? toVcuser.getNickname() : "");
        resMine.setBio(StringUtils.isNotEmpty(toVcuser.getBio()) ? toVcuser.getBio() : "");
        resMine.setAvatar(imageHostUtil.absImagePath(toVcuser.getAvatar()));
        resMine.setCover(imageHostUtil.absImagePath(toVcuser.getCover()));
        // =========================== 用户信息 ===========================

        resMine.setFans_count(fans_count);
        resMine.setFollow_count(follow_count);
        resMine.setFollow_type(follow_type);
        resMine.setIs_vip(is_vip);
        resMine.setStar_count(star_count);
        resMine.setPersons(personList);
        resMine.setIdol_count(idol_count);
        resMine.setCheck_days_count(check_days_count);
        if (mine_vcuser_id != null && mine_vcuser_id == to_vcuser_id) {
            if (redisUtil.hasKey("blog_new_star:" + mine_vcuser_id)) {
                int newStar = (int) redisUtil.get("blog_new_star:" + mine_vcuser_id);
                resMine.setNew_star(newStar);
            }
        }
        return R.OK(resMine);
    }


    public R<ResMine> getUser(int userId) {
        XunyeeVcuser vcuser = new XunyeeVcuser().selectById(userId);

        // ===============  是不是会员  ====================
        LocalDate nowDate = LocalDate.now();
        LambdaQueryWrapper<XunyeeVcuserBenefit> qw = new LambdaQueryWrapper<>();
        qw.eq(XunyeeVcuserBenefit::getVcuser_id, userId)
                .le(XunyeeVcuserBenefit::getStart_time, nowDate)
                .ge(XunyeeVcuserBenefit::getFinish_time, nowDate);
        XunyeeVcuserBenefit vcuserBenefit = new XunyeeVcuserBenefit().selectOne(qw);
        boolean is_vip = vcuserBenefit != null;
        int vipDays = -1;//表示非会员
        if (is_vip) {
            //Duration duration = Duration.between(nowDate,vcuserBenefit.getFinish_time());
            vipDays = (int) (vcuserBenefit.getFinish_time().plusDays(1).toEpochDay() - nowDate.toEpochDay()); //相差的天数
        }

        // ===============  是不是会员  ====================

        // ===============  我的关注  ====================
        LambdaQueryWrapper<XunyeeFollow> fqw = new LambdaQueryWrapper<>();
        fqw.eq(XunyeeFollow::getVcuser_id, userId)
                .eq(XunyeeFollow::getStatus, 1);
        int follow_count = new XunyeeFollow().selectCount(fqw);
        // ===============  我的关注  ====================

        // ===============  我的粉丝  ====================
        LambdaQueryWrapper<XunyeeFollow> tqw = new LambdaQueryWrapper<>();
        tqw.eq(XunyeeFollow::getFollowed_vcuser_id, userId)
                .eq(XunyeeFollow::getStatus, 1);
        int fans_count = new XunyeeFollow().selectCount(tqw);
        // ===============  我的粉丝  ====================

        // ===============  我的点赞  ====================
        String sql = "SELECT COALESCE(sum(star_count),0) star_count FROM xunyee_blog where vcuser_id=" + userId;
        int star_count = jdbcTemplate.queryForObject(sql, int.class);
        // ===============  我的点赞  ====================

        // ===============  我的爱豆  ====================
        Criteria criteria = new Criteria().where("vcuser").is(userId).and("is_enabled").is(true);
        Query query = new Query(criteria);
        // 我的爱豆总数
        int idol_count = (int) mongoTemplate.count(query, ResMonUserPerson.class);
        // 分页 排序
        query.limit(3).with(Sort.by(Sort.Direction.DESC, "updated"));
        // 查询我关注的艺人
        List<ResMonUserPerson> resMGs = mongoTemplate.find(query, ResMonUserPerson.class);
        // 提取teleplay id去数据库查询电视剧信息
        Integer[] personIds = resMGs.stream().map(e -> e.getPerson()).collect(Collectors.toList())
                .toArray(new Integer[resMGs.size()]);
        // 查询数据库艺人信息
        List<Person> persons = personIds.length > 0 ? metaService.getPerson(personIds) : new ArrayList<>();
        List<String> avatarList = new ArrayList<>();
        for (Person person : persons) {
            avatarList.add(imageHostUtil.absImagePath(person.getAvatar_custom()));
        }
        // ===============  我的爱豆  ====================

        // ===============  品牌浏览数  ====================
        LambdaQueryWrapper<XunyeeBrandBrowsingHistory> brandQw = new LambdaQueryWrapper<>();
        brandQw.eq(XunyeeBrandBrowsingHistory::getVcuser_id, userId);
        int brandCount = new XunyeeBrandBrowsingHistory().selectCount(brandQw);
        // ===============  品牌浏览数  ====================

        ResMine resMine = new ResMine();
        resMine.setVcuser_id(vcuser.getId());
        resMine.setAvatar(imageHostUtil.absImagePath(vcuser.getAvatar()));
        resMine.setNickname(vcuser.getNickname());
        resMine.setFans_count(fans_count);
        resMine.setFollow_count(follow_count);
        resMine.setIdol_count(idol_count);
        resMine.setPersons(avatarList);
        resMine.setIs_vip(is_vip);
        resMine.setVip_days(vipDays);
        resMine.setBrand_brow_count(brandCount);
        resMine.setStar_count(star_count);

        if (redisUtil.hasKey("blog_new_star:" + userId)) {
            int newStar = (int) redisUtil.get("blog_new_star:" + userId);
            resMine.setNew_star(newStar);
        }
        return R.OK(resMine);
    }

    public R editUser(int userId, ReqUserInfo req) {
        XunyeeVcuser vcuser = new XunyeeVcuser();
        if (StringUtils.isNotEmpty(req.getBio())) {
            vcuser.setBio(req.getBio());
        }

        if (StringUtils.isNotEmpty(req.getNickname())) {
            vcuser.setNickname(req.getNickname());
        }

        if (StringUtils.isNotEmpty(req.getAvatar())) {
            // 防止前端传 绝对路径的图片
            vcuser.setAvatar(imageHostUtil.removeImagePath(req.getAvatar()));
        }

        if (StringUtils.isNotEmpty(req.getWx_avatar())) {
            vcuser.setWx_avatar(req.getWx_avatar());
        }

        if (req.getSex() != null) {
            vcuser.setSex(req.getSex());
        }

        if (StringUtils.isNotEmpty(req.getWx_country())) {
            vcuser.setWx_country(req.getWx_country());
        }

        if (StringUtils.isNotEmpty(req.getWx_city())) {
            vcuser.setWx_city(req.getWx_city());
        }

        if (StringUtils.isNotEmpty(req.getWx_province())) {
            vcuser.setWx_province(req.getWx_province());
        }

        if (vcuser == null) {
            return R.ERROR("未修改任何信息");
        }


        vcuser.setId(userId);
        vcuser.setUpdated(new Date());

        if (vcuserMapper.updateById(vcuser) > 0) {
            return R.OK();
        }
        return R.ERROR("修改失败");
    }

    public R follow(Integer from_userid, int vcuser_id) {

        if (from_userid == vcuser_id) {
            return R.ERROR("不能关注自己");
        }
        // ===============  我是否以前关注过该用户  ====================
        LambdaQueryWrapper<XunyeeFollow> fqw = new LambdaQueryWrapper<>();
        fqw.eq(XunyeeFollow::getFollowed_vcuser_id, vcuser_id)
                .eq(XunyeeFollow::getVcuser_id, from_userid);
        XunyeeFollow temp = new XunyeeFollow().selectOne(fqw);
        // ===============  我是否以前关注过该用户  ====================

        // ===============  对方是否关注了我 status=1  ====================
        LambdaQueryWrapper<XunyeeFollow> tqw = new LambdaQueryWrapper<>();
        tqw.eq(XunyeeFollow::getFollowed_vcuser_id, from_userid)
                .eq(XunyeeFollow::getVcuser_id, vcuser_id)
                .eq(XunyeeFollow::getStatus, 1);
        XunyeeFollow toFollow = new XunyeeFollow().selectOne(tqw);
        // ===============  对方是否关注了我 status=1 ====================


        if (temp != null) {//如果当前用户之前关注过该用户
            int status = temp.getStatus();
            if (status == 0) {
                temp.setStatus(1);
                if (toFollow != null && toFollow.getType() != 3) {
                    temp.setType(3);//互相关注
                    toFollow.setType(3);//互相关注
                    toFollow.updateById();//更新对方的关注状态为互相关注
                } else {
                    temp.setType(1);
                }
            } else {
                temp.setStatus(0);//取关
                temp.setType(1);
                if (toFollow != null) {
                    toFollow.setType(1);//改为关注
                    toFollow.updateById();//更新对方的关注状态为互相关注
                }
            }

            temp.setUpdated(new Date());
            if (temp.updateById()) {
                return R.OK();
            }
        } else { //当前用户并没有关注该用户
            XunyeeFollow follow = new XunyeeFollow();
            follow.setFollowed_vcuser_id(vcuser_id);
            follow.setVcuser_id(from_userid);
            follow.setStatus(1);//关注
            if (toFollow != null) {
                follow.setType(3);//互相关注状态
                toFollow.setType(3);
                toFollow.updateById();//更新对方的关注状态为互相关注
            } else {
                follow.setType(1);//关注状态
            }

            if (follow.insert()) {
                return R.OK();
            }
        }
        return R.ERROR("关注失败");
    }

    public R<IPage<ResFollowPage>> getFollows(Integer vcuser_id, ReqPageFollow req) {
        Page page = new Page(req.getCurrent(), req.getSize());
        IPage<ResFollowPage> iPage = myMapper.selectFollowPage(page, req.getType(), vcuser_id);
        for (ResFollowPage record : iPage.getRecords()) {
            record.setAvatar(imageHostUtil.absImagePath(record.getAvatar()));
        }
        return R.OK(iPage);
    }

    public R report(int userId, ReqUserReport req) {
        XunyeeVcuserReport report = BeanUtil.copyProperties(req, XunyeeVcuserReport.class);
        report.setVcuser_id(userId);
        if (report.insert()) {
            return R.OK();
        }
        return R.ERROR("举报失败");
    }

    public R<IPage<ResPerson>> vcuserPerson(int userId, ReqMyPage myPage) {
        int current = myPage.getCurrent();
        int size = myPage.getSize();


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

        IPage<ResPerson> iPage = new Page();
        iPage.setPages(totalPage);
        iPage.setSize(size);
        iPage.setCurrent(current);
        iPage.setTotal(totalCount);
        iPage.setRecords(CopyListUtil.copyListProperties(persons, ResPerson.class));
        return R.OK(iPage);
    }

    public R<IPage<ResBlogStarPage>> getBlogStar(int userId, ReqMyPage myPage) {
        //先删除新的点赞数据
        redisUtil.setRemove("blog_new_star:"+userId);
        Page page = new Page(myPage.getCurrent(), myPage.getSize());
        IPage<ResBlogStarPage> iPage = myMapper.selectBlogStarPage(page, userId);
        for (ResBlogStarPage record : iPage.getRecords()) {
            record.setAvatar(imageHostUtil.absImagePath(record.getAvatar()));
            if (StringUtils.isNotEmpty(record.getImages())) {
                String[] s = record.getImages().split(",");
                List<String> image_list = new ArrayList<>();
                for (int i = 0; i < s.length; i++) {
                    image_list.add(imageHostUtil.absImagePath(s[i]));
                }

                record.setImage_list(image_list);
            }
        }
        return R.OK(iPage);

    }

    public R uploadCover(int userId, MultipartFile file) throws IOException {
        R<String> result = uploadService.qiNiuYunUploadImage(file, "user/cover/");
        if (result.getCode() == 0) {
            UpdateWrapper<XunyeeVcuser> wrapper = new UpdateWrapper<>();
            wrapper.eq("id", userId).set("cover", result.getData());
            boolean isUpdate = new XunyeeVcuser().update(wrapper);
            if (isUpdate) {
                return R.OK();
            }
            return R.ERROR("封面图上传失败");
        }
        return R.ERROR("更新用户封面图失败");
    }

    public R uploaduploadCoverDefaultCover(int userId) {
        UpdateWrapper<XunyeeVcuser> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", userId).set("cover", "");
        boolean isUpdate = new XunyeeVcuser().update(wrapper);
        if (isUpdate) {
            return R.OK();
        }
        return R.ERROR("封面图上传失败");
    }

    public R blogStarFavoriteBrow(int userId, ReqMyPage myPage, int type) {
        Page page = new Page(myPage.getCurrent(), myPage.getSize());
        IPage<ResMyBlogStarPage> iPage = null;
        switch (type) {
            case 1:
                iPage = myMapper.selectMyBlogStarPage(page, userId);
                break;
            case 2:
                iPage = myMapper.selectMyBlogFavoritePage(page, userId);
                break;
            case 3:
                iPage = myMapper.selectMyBlogBrowHistoryPage(page, userId);
                break;
        }

        for (ResMyBlogStarPage record : iPage.getRecords()) {
            record.setAvatar(imageHostUtil.absImagePath(record.getAvatar()));
            record.setCover(imageHostUtil.absImagePath(record.getCover()));
        }
        return R.OK(iPage);
    }
}
