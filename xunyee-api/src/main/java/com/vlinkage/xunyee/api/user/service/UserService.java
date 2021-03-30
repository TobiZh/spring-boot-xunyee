package com.vlinkage.xunyee.api.user.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysql.cj.xdevapi.InsertResultImpl;
import com.vlinkage.ant.meta.entity.Person;
import com.vlinkage.ant.xunyee.entity.*;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.meta.service.MetaService;
import com.vlinkage.xunyee.entity.ReqMyPage;
import com.vlinkage.xunyee.entity.request.ReqBlogReport;
import com.vlinkage.xunyee.entity.request.ReqPageFollow;
import com.vlinkage.xunyee.entity.request.ReqUserInfo;
import com.vlinkage.xunyee.entity.request.ReqUserReport;
import com.vlinkage.xunyee.entity.response.*;
import com.vlinkage.xunyee.mapper.MyMapper;
import com.vlinkage.xunyee.utils.CopyListUtil;
import com.vlinkage.xunyee.utils.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Value("${sys-config.image-path}")
    private String imagePath;

    @Autowired
    private MyMapper myMapper;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MetaService metaService;


    public R<ResUserInfoOhter> other(Integer mine_vcuser_id, Integer userId) {

        XunyeeVcuser vcuser=new XunyeeVcuser().selectById(userId);


        int follow_type=0;
        List<String> personList=new ArrayList<>();

        if (mine_vcuser_id!=null){
            // ===============  我是否以前关注过该用户  ====================
            //我是否关注过对方
            QueryWrapper fqw=new QueryWrapper();
            fqw.eq("followed_vcuser_id",userId);
            fqw.eq("vcuser_id",mine_vcuser_id);
            fqw.eq("status",1);
            XunyeeFollow xunyeeFollow=new XunyeeFollow().selectOne(fqw);

            //对方是否关注了我
            QueryWrapper tqw=new QueryWrapper();
            tqw.eq("followed_vcuser_id",mine_vcuser_id);
            tqw.eq("vcuser_id",userId);
            tqw.eq("status",1);
            XunyeeFollow tFollow=new XunyeeFollow().selectOne(tqw);

            // ===============  我是否以前关注过该用户  ====================

            // ===============  共同关注  ====================
            // 查询对方关注的艺人
            Criteria criteria = new Criteria().where("vcuser").is(userId).and("is_enabled").is(true);
            Query query = new Query(criteria);
            List<ResMonUserPerson> resMGs = mongoTemplate.find(query, ResMonUserPerson.class);

            // 查询我关注的艺人
            Criteria criteriaMine = new Criteria().where("vcuser").is(mine_vcuser_id).and("is_enabled").is(true);
            Query queryMine = new Query(criteriaMine);
            List<ResMonUserPerson> resMGsMine = mongoTemplate.find(queryMine, ResMonUserPerson.class);

            // 提取person id去数据库查询电视剧信息并取出并集
            List<Integer> personIds = resMGs.stream().map(e -> e.getPerson()).collect(Collectors.toList());
            List<Integer> personIdsMine = resMGsMine.stream().map(e -> e.getPerson()).collect(Collectors.toList());
            List<Integer> intersection = personIds.stream().filter(item -> personIdsMine.contains(item)).collect(Collectors.toList());

            if (intersection.size()>0){
                // 查询数据库艺人信息
                Integer[] personIdArr=new Integer[intersection.size()];
                List<Person> persons = metaService.getPerson(personIdArr);
                for (Person person : persons) {
                    personList.add(person.getZh_name());
                }
            }
            // ===============  共同关注  ====================

        }

        // ===============  是不是会员  ====================
        LocalDate gteDate = LocalDate.now(); // >=
        LocalDate ltDate = gteDate.plusDays(1); // <; // <

        QueryWrapper qw = new QueryWrapper();
        qw.eq("vcuser_id", userId);
        qw.ge("start_time", gteDate);//<=
        qw.lt("finish_time", ltDate);// >
        XunyeeVcuserBenefit vcuserBenefit = new XunyeeVcuserBenefit().selectOne(qw);
        boolean is_vip=vcuserBenefit==null;
        // ===============  是不是会员  ====================

        // ===============  我的关注  ====================
        QueryWrapper fqw=new QueryWrapper();
        fqw.eq("vcuser_id",userId);
        fqw.eq("status",1);
        int follow_count=new XunyeeFollow().selectCount(fqw);
        // ===============  我的关注  ====================

        // ===============  我的粉丝  ====================
        QueryWrapper tqw=new QueryWrapper();
        tqw.eq("followed_vcuser_id",userId);
        tqw.eq("status",1);
        int fans_count=new XunyeeFollow().selectCount(tqw);
        // ===============  我的粉丝  ====================

        // ===============  我的点赞  ====================
        String sql="SELECT COALESCE(sum(star_count),0) star_count FROM xunyee_blog where vcuser_id="+userId;
        int star_count=jdbcTemplate.queryForObject(sql,int.class);
        // ===============  我的点赞  ====================


        ResUserInfoOhter resMine=new ResUserInfoOhter();
        resMine.setAvatar(vcuser.getAvatar());
        resMine.setNickname(vcuser.getNickname());
        resMine.setFans_count(fans_count);
        resMine.setFollow_count(follow_count);
        resMine.setFollow_type(follow_type);
        resMine.setIs_vip(is_vip);
        resMine.setStar_count(star_count);
        resMine.setPersons(personList);
        return R.OK(resMine);
    }


    public R<ResMine> getUser(int userId) {
        XunyeeVcuser vcuser=new XunyeeVcuser().selectById(userId);

        // ===============  是不是会员  ====================
        LocalDate gteDate = LocalDate.now(); // >=
        LocalDate ltDate = gteDate.plusDays(1); // <; // <

        QueryWrapper qw = new QueryWrapper();
        qw.eq("vcuser_id", userId);
        qw.ge("start_time", gteDate);//<=
        qw.lt("finish_time", ltDate);// >
        XunyeeVcuserBenefit vcuserBenefit = new XunyeeVcuserBenefit().selectOne(qw);
        boolean is_vip=vcuserBenefit==null;
        // ===============  是不是会员  ====================

        // ===============  我的关注  ====================
        QueryWrapper fqw=new QueryWrapper();
        fqw.eq("vcuser_id",userId);
        fqw.eq("status",1);
        int follow_count=new XunyeeFollow().selectCount(fqw);
        // ===============  我的关注  ====================

        // ===============  我的粉丝  ====================
        QueryWrapper tqw=new QueryWrapper();
        tqw.eq("followed_vcuser_id",userId);
        tqw.eq("status",1);
        int fans_count=new XunyeeFollow().selectCount(tqw);
        // ===============  我的粉丝  ====================

        // ===============  我的点赞  ====================
        String sql="SELECT COALESCE(sum(star_count),0) star_count FROM xunyee_blog where vcuser_id="+userId;
        int star_count=jdbcTemplate.queryForObject(sql,int.class);
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
        List<String> avatarList=new ArrayList<>();
        for (Person person : persons) {
            avatarList.add(imagePath+person.getAvatar_custom());
        }
        // ===============  我的爱豆  ====================


        ResMine resMine=new ResMine();
        resMine.setVcuser_id(vcuser.getId());
        resMine.setAvatar(vcuser.getAvatar());
        resMine.setNickname(vcuser.getNickname());
        resMine.setFans_count(fans_count);
        resMine.setFollow_count(follow_count);
        resMine.setIdol_count(idol_count);
        resMine.setPersons(avatarList);
        resMine.setIs_vip(is_vip);
        resMine.setStar_count(star_count);
        return R.OK(resMine);
    }

    public R editUser(int userId, ReqUserInfo req) {
        XunyeeVcuser vcuser=new XunyeeVcuser().selectById(userId);
        if (vcuser==null){
            return R.ERROR("用户不存在");
        }
        BeanUtil.copyProperties(req,vcuser);

        if (vcuser.updateById()){
            return R.OK();
        }
        return R.ERROR("修改失败");
    }

    public R follow(Integer from_userid, int vcuser_id) {

        if (from_userid==vcuser_id){
            return R.ERROR("不能关注自己");
        }
        // ===============  我是否以前关注过该用户  ====================
        QueryWrapper fqw=new QueryWrapper();
        fqw.eq("followed_vcuser_id",vcuser_id);
        fqw.eq("vcuser_id",from_userid);
        XunyeeFollow temp=new XunyeeFollow().selectOne(fqw);
        // ===============  我是否以前关注过该用户  ====================

        // ===============  对方是否关注了我  ====================
        QueryWrapper tqw=new QueryWrapper();
        tqw.eq("followed_vcuser_id",from_userid);
        tqw.eq("vcuser_id",vcuser_id);
        tqw.eq("status",1);
        XunyeeFollow toFollow=new XunyeeFollow().selectOne(tqw);
        // ===============  对方是否关注了我  ====================


        if (temp!=null){//如果当前用户之前关注过该用户
            int status=temp.getStatus();
            if (status==0){
                temp.setStatus(1);
                if(toFollow!=null&&toFollow.getType()!=3){
                    temp.setType(3);//互相关注
                    toFollow.setType(3);//互相关注
                    toFollow.updateById();//更新对方的关注状态为互相关注
                }else{
                    temp.setType(1);
                }
            }else{
                temp.setStatus(0);//取关
                temp.setType(1);
                if(toFollow!=null){
                    toFollow.setType(1);//改为关注
                    toFollow.updateById();//更新对方的关注状态为互相关注
                }
            }

            temp.setUpdated(new Date());
            if(temp.updateById()){
                return R.OK();
            }
        }else{ //当前用户并没有关注该用户
            XunyeeFollow follow=new XunyeeFollow();
            follow.setFollowed_vcuser_id(vcuser_id);
            follow.setVcuser_id(from_userid);
            follow.setStatus(1);//关注
            if(toFollow!=null){
                follow.setType(3);//互相关注状态
                toFollow.setType(3);
                toFollow.updateById();//更新对方的关注状态为互相关注
            }else{
                follow.setType(1);//关注状态
            }

            if(follow.insert()){
                return R.OK();
            }
        }
        return R.ERROR("关注失败");
    }

    public R<IPage<ResFollowPage>> getFollows(Integer vcuser_id, ReqPageFollow req) {
        Page page=new Page(req.getCurrent(),req.getSize());
        IPage<ResFollowPage> iPage=myMapper.selectFollowPage(page,req.getType(),vcuser_id);
        return R.OK(iPage);
    }

    public R report(int userId, ReqUserReport req) {
        XunyeeVcuserReport report=BeanUtil.copyProperties(req, XunyeeVcuserReport.class);
        report.setVcuser_id(userId);
        if (report.insert()){
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

        IPage<ResPerson> iPage=new Page();
        iPage.setPages(totalPage);
        iPage.setSize(size);
        iPage.setCurrent(current);
        iPage.setTotal(totalCount);
        iPage.setRecords(CopyListUtil.copyListProperties(persons,ResPerson.class));
        return R.OK(iPage);
    }

    public R<IPage<ResBlogStarPage>> getBlogStar(int userId, ReqMyPage myPage) {
        Page page=new Page(myPage.getCurrent(),myPage.getSize());
        IPage<ResBlogStarPage> iPage=myMapper.selectBlogStarPage(page,userId);
        for (ResBlogStarPage record : iPage.getRecords()) {
            if (StringUtils.isNotEmpty(record.getImages())){
                String[] s=record.getImages().split(",");
                List<String> reImages=new ArrayList<>();
                for (int i = 0; i < s.length; i++) {
                    reImages.add(imagePath+s[i]);
                }
                String newStr = reImages.stream().collect(Collectors.joining(","));
                record.setImages(newStr);
            }
        }

        return R.OK(iPage);

    }
}
