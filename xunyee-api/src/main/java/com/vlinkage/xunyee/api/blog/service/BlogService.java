package com.vlinkage.xunyee.api.blog.service;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vlinkage.ant.meta.entity.Person;
import com.vlinkage.ant.xunyee.entity.*;
import com.vlinkage.xunyee.api.provide.MetaService;
import com.vlinkage.xunyee.config.redis.RedisUtil;
import com.vlinkage.xunyee.entity.ReqMyPage;
import com.vlinkage.xunyee.entity.request.*;
import com.vlinkage.xunyee.entity.response.ResBlogInfo;
import com.vlinkage.xunyee.entity.response.ResBlogPage;
import com.vlinkage.xunyee.entity.response.ResBlogStar;
import com.vlinkage.xunyee.entity.response.ResBrandNameUrl;
import com.vlinkage.xunyee.entity.result.R;
import com.vlinkage.xunyee.mapper.MyMapper;
import com.vlinkage.xunyee.utils.ImageHostUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class BlogService {

    @Autowired
    private ImageHostUtil imageHostUtil;

    @Autowired
    private MyMapper myMapper;

    @Autowired
    private MetaService metaService;

    @Autowired
    private RedisUtil redisUtil;


    public R blog(int userId, ReqBlog req) {
        LambdaQueryWrapper<XunyeeVcuser> qw=new LambdaQueryWrapper<>();
        qw.select(XunyeeVcuser::getStatus)
            .eq(XunyeeVcuser::getId,userId);
        XunyeeVcuser vcuser=new XunyeeVcuser().selectOne(qw);
        if (vcuser.getStatus()==1){
            return R.ERROR("你已被锁定，不可发布动态、点赞点踩");
        }
        if (req.getImages().split(",").length > 9) {
            return R.ERROR("图片最多上传9张");
        }

        XunyeeBlog xunyeeBlog = new XunyeeBlog();
        BeanUtil.copyProperties(req, xunyeeBlog);
        xunyeeBlog.setVcuser_id(userId);
        if (xunyeeBlog.insert()) {
            return R.OK(xunyeeBlog.getId());
        }
        return R.ERROR("发布失败");
    }

    public R<IPage<ResBlogPage>> getBlogByUserId(Integer userId, ReqPageBlogUser req) {
        Page page = new Page(req.getCurrent(), req.getSize());
        IPage<ResBlogPage> iPage = myMapper.selectUserBlogPage(page, req.getVcuser_id(), userId);
        for (ResBlogPage record : iPage.getRecords()) {
            record.setAvatar(imageHostUtil.absImagePath(record.getAvatar()));
            if (StringUtils.isNotEmpty(record.getCover())) {
                record.setCover(imageHostUtil.absImagePath(record.getCover()));
            }
        }

        return R.OK(iPage);
    }

    public R<IPage<ResBlogPage>> getMineBlog(ReqMyPage myPage, int userId, String name) {

        Page page = new Page(myPage.getCurrent(), myPage.getSize());
        IPage<ResBlogPage> iPage = myMapper.selectMineBlogPage(page, userId, name);
        for (ResBlogPage record : iPage.getRecords()) {
            record.setAvatar(imageHostUtil.absImagePath(record.getAvatar()));
            if (StringUtils.isNotEmpty(record.getCover())) {
                record.setCover(imageHostUtil.absImagePath(record.getCover()));
            }
        }

        return R.OK(iPage);
    }


    public R<IPage<ResBlogPage>> blogCategory(ReqMyPage myPage, Integer type, Integer userId) {
        Page page = new Page(myPage.getCurrent(), myPage.getSize());
        IPage<ResBlogPage> iPage = myMapper.selectBlogCategoryPage(page, type, userId);
        for (ResBlogPage record : iPage.getRecords()) {
            record.setAvatar(imageHostUtil.absImagePath(record.getAvatar()));
            if (StringUtils.isNotEmpty(record.getCover())) {
                record.setCover(imageHostUtil.absImagePath(record.getCover()));
            }
        }
        return R.OK(iPage);
    }

    public R<IPage<ResBlogPage>> blogFollow(ReqMyPage myPage, int userId) {
        Page page = new Page(myPage.getCurrent(), myPage.getSize());
        IPage<ResBlogPage> iPage = myMapper.selectBlogFollowPage(page, userId);
        for (ResBlogPage record : iPage.getRecords()) {
            record.setAvatar(imageHostUtil.absImagePath(record.getAvatar()));
            if (StringUtils.isNotEmpty(record.getCover())) {
                record.setCover(imageHostUtil.absImagePath(record.getCover()));
            }
        }
        return R.OK(iPage);
    }

    public R<ResBlogInfo> blogInfo(Integer userId, Integer blogId) {
        ResBlogInfo info = new ResBlogInfo();

        // 动态信息
        XunyeeBlog blog = new XunyeeBlog().selectById(blogId);
        info.setBlog_id(blog.getId());
        info.setTitle(blog.getTitle());
        info.setContent(blog.getContent());

        List<String> imageList = new ArrayList<>();
        if (StringUtils.isNotEmpty(blog.getImages())) {
            String[] imageArr = blog.getImages().split(",");
            for (String s : imageArr) {
                imageList.add(imageHostUtil.absImagePath(s));
            }
        }
        info.setImage_list(imageList);

        if (blog.getType() == 3 && blog.getType_id()!=null) {//品牌 需要读取品牌名称
            ResBrandNameUrl resBrandNameUrl = metaService.getBrandNameUrlById(blog.getType_id(),blog.getPerson_id());
            if(resBrandNameUrl!=null){
                info.setBrand_name(resBrandNameUrl.getName());
                info.setBrand_url(resBrandNameUrl.getUrl());
            }
        }
        info.setCreated(blog.getCreated());
        info.setType(blog.getType());
        info.setType_id(blog.getType_id());
        info.setPerson_id(blog.getPerson_id());

        info.setStar_count(blog.getStar_count());
        info.setUnstar_count(blog.getUnstar_count());
        info.setFavorite_count(blog.getFavorite_count());

        // 用户信息
        XunyeeVcuser vcuser = new XunyeeVcuser().selectById(blog.getVcuser_id());
        info.setVcuser_id(vcuser.getId());
        info.setNickname(vcuser.getNickname());
        info.setAvatar(imageHostUtil.absImagePath(vcuser.getAvatar()));

        // 相关艺人
        Person person = metaService.getPersonById(blog.getPerson_id());
        info.setPerson_name(person.getZh_name());
        info.setPerson_avatar_customer(imageHostUtil.absImagePath(person.getAvatar_custom()));

        // 是否 点赞 点踩 收藏 关注状态
        boolean isStar = false;
        boolean isUnStar = false;
        boolean isFavorite = false;
        int follow_type = 0;
        if (userId != null) {
            LambdaQueryWrapper<XunyeeBlogStar> sqw = new LambdaQueryWrapper<>();
            sqw.eq(XunyeeBlogStar::getVcuser_id, userId)
                    .eq(XunyeeBlogStar::getBlog_id, blogId)
                    .eq(XunyeeBlogStar::getStatus, 1)
                    .eq(XunyeeBlogStar::getType, 1);
            isStar = new XunyeeBlogStar().selectCount(sqw) > 0;

            LambdaQueryWrapper<XunyeeBlogStar> unqw = new LambdaQueryWrapper<>();
            unqw.eq(XunyeeBlogStar::getVcuser_id, userId)
                    .eq(XunyeeBlogStar::getBlog_id, blogId)
                    .eq(XunyeeBlogStar::getStatus, 1)
                    .eq(XunyeeBlogStar::getType, 0);
            isUnStar = new XunyeeBlogStar().selectCount(unqw) > 0;

            // 收藏
            LambdaQueryWrapper<XunyeeBlogFavorite> faqw = new LambdaQueryWrapper<>();
            faqw.eq(XunyeeBlogFavorite::getVcuser_id, userId)
                    .eq(XunyeeBlogFavorite::getBlog_id, blogId)
                    .eq(XunyeeBlogFavorite::getStatus, 1);
            isFavorite = new XunyeeBlogFavorite().selectCount(faqw) > 0;


            // ===============  我是否以前关注过该用户 follow_type 关注状态 ====================
            LambdaQueryWrapper<XunyeeFollow> foqw = new LambdaQueryWrapper<>();
            foqw.eq(XunyeeFollow::getVcuser_id, userId)
                    .eq(XunyeeFollow::getFollowed_vcuser_id, blog.getVcuser_id())
                    .eq(XunyeeFollow::getStatus, 1)
                    .or()
                    .eq(XunyeeFollow::getVcuser_id,blog.getVcuser_id())
                    .eq(XunyeeFollow::getFollowed_vcuser_id,userId)
                    .eq(XunyeeFollow::getStatus, 1);
            List<XunyeeFollow> follows = new XunyeeFollow().selectList(foqw);
            if (follows.size()>0) {
                for (XunyeeFollow follow : follows) {
                    if (follow.getType()==3){
                        follow_type = follow.getType();
                    }else{
                        if (follow.getVcuser_id()==blog.getVcuser_id()){
                            follow_type = follow.getType();
                        }else{
                            follow_type = 2;//回关
                        }
                    }
                }

            }
            // ===============  我是否以前关注过该用户  ====================


            // 添加一条浏览记录
            LambdaQueryWrapper<XunyeeBlogBrowsingHistory> hqw=new LambdaQueryWrapper();
            hqw.eq(XunyeeBlogBrowsingHistory::getBlog_id,blogId)
                    .eq(XunyeeBlogBrowsingHistory::getVcuser_id,userId);
            XunyeeBlogBrowsingHistory temp=new XunyeeBlogBrowsingHistory().selectOne(hqw);
            if (temp==null){
                XunyeeBlogBrowsingHistory history=new XunyeeBlogBrowsingHistory();
                history.setVcuser_id(userId);
                history.setBlog_id(blogId);
                history.insert();
            }else{
                temp.setLast_brow_time(new Date());
                temp.updateById();
            }


        }
        info.setIs_star(isStar);
        info.setIs_unstar(isUnStar);
        info.setIs_favorite(isFavorite);
        info.setFollow_type(follow_type);
        return R.OK(info);
    }

    public R<IPage<ResBlogPage>> recommend(ReqMyPage myPage, ReqRecommendPage req, Integer vcuser_id) {
        Page page = new Page(myPage.getCurrent(), myPage.getSize());
        IPage<ResBlogPage> iPage = myMapper.selectRecommendBlogPage(page, vcuser_id, req);
        for (ResBlogPage record : iPage.getRecords()) {
            if (StringUtils.isNotEmpty(record.getCover())) {
                record.setCover(imageHostUtil.absImagePath(record.getCover()));
            }
        }
        return R.OK(iPage);
    }

    @Transactional
    public R<ResBlogStar> blogStar(Integer userId, ReqBlogStar req) {
        LambdaQueryWrapper<XunyeeVcuser> userQw=new LambdaQueryWrapper<>();
        userQw.select(XunyeeVcuser::getStatus)
                .eq(XunyeeVcuser::getId,userId);
        XunyeeVcuser vcuser=new XunyeeVcuser().selectOne(userQw);
        if (vcuser.getStatus()==1){
            return R.ERROR("你已被锁定，不可发布动态、点赞点踩");
        }
        int blogId = req.getBlog_id();
        int type = req.getType();
        ResBlogStar resBlogStar=new ResBlogStar();

        LambdaQueryWrapper<XunyeeBlogStar> qw = new LambdaQueryWrapper<>();
        qw.eq(XunyeeBlogStar::getVcuser_id, userId)
                .eq(XunyeeBlogStar::getBlog_id, blogId);
        XunyeeBlogStar temp = new XunyeeBlogStar().selectOne(qw);
        if (temp == null) {
            XunyeeBlogStar blogStar = new XunyeeBlogStar();
            blogStar.setVcuser_id(userId);
            blogStar.setBlog_id(blogId);
            blogStar.setType(type);
            blogStar.setStatus(1);
            blogStar.setUpdated(new Date());
            if (blogStar.insert()) {
                // 该动态添加 star_count计数+1
                XunyeeBlog blog = new XunyeeBlog().selectById(blogId);
                if (type == 0) {
                    //点踩
                    resBlogStar.setUnstar_count(blog.getUnstar_count());//点踩数量
                    blog.setUnstar_count(blog.getUnstar_count() + 1);
                } else if (type == 1) {
                    //点赞
                    resBlogStar.setIs_star(true);
                    blog.setStar_count(blog.getStar_count() + 1);
                    redisUtil.incr("blog_new_star:"+blog.getVcuser_id(),1);
                }
                blog.updateById();
                resBlogStar.setId(blog.getId());
                resBlogStar.setStar_count(blog.getStar_count());
                return R.OK(resBlogStar);
            }
        } else {
            int tempType = temp.getType();//获取之前的type
            int tempStatus = temp.getStatus();//获取之前的status

            if ((type == 0 && tempType == 0) || (type == 1 && tempType == 1)) {//点踩 点赞
                temp.setStatus(tempStatus == 0 ? 1 : 0);
            } else if ((tempType == 0 && type == 1) || (tempType == 1 && type == 0)) {//点踩-点赞 点赞-点踩
                temp.setStatus(1);
            }
            temp.setUpdated(new Date());
            temp.setType(type);
            if (temp.updateById()) {
                // 该动态添加 star_count计数+1
                XunyeeBlog blog = new XunyeeBlog().selectById(blogId);
                int starCount = blog.getStar_count();
                int unStarCount = blog.getUnstar_count();
                if (type == 0 && tempType == 0) {//点踩
                    if (temp.getStatus() == 0) {
                        //resultStr = "取消点踩成功";
                        blog.setUnstar_count(unStarCount - 1);
                    } else {
                        //resultStr = "点踩成功";
                        blog.setUnstar_count(unStarCount + 1);
                        resBlogStar.setIs_unstar(true);
                    }
                } else if (type == 1 && tempType == 1) {//点赞
                    if (temp.getStatus() == 0) {
                        //resultStr = "取消点赞成功";
                        blog.setStar_count(starCount - 1);
                    } else {
                        //resultStr = "点赞成功";
                        blog.setStar_count(starCount + 1);
                        resBlogStar.setIs_star(true);
                        redisUtil.incr("blog_new_star:"+blog.getVcuser_id(),1);
                    }
                } else if (tempType == 0 && type == 1) {//点踩 > 点赞
                    blog.setStar_count(starCount + 1);
                    if (tempStatus == 1) {
                        blog.setUnstar_count(unStarCount - 1);
                    }
                    //resultStr = "点赞成功";
                    resBlogStar.setIs_star(true);
                    redisUtil.incr("blog_new_star:"+blog.getVcuser_id(),1);
                } else if (tempType == 1 && type == 0) {//点赞 > 点踩
                    blog.setUnstar_count(unStarCount + 1);
                    if (tempStatus == 1) {
                        blog.setStar_count(starCount - 1);
                    }
                    //resultStr = "点踩成功";
                    resBlogStar.setIs_unstar(true);
                }
                blog.updateById();
                resBlogStar.setId(blog.getId());
                resBlogStar.setStar_count(blog.getStar_count());//点赞数量
                resBlogStar.setUnstar_count(blog.getUnstar_count());//点踩数量
                return R.OK(resBlogStar);
            }
        }
        return R.ERROR(type == 0 ? "点踩失败" : "点赞失败");
    }


    @Transactional
    public R blogFavorite(Integer userId, int blogId) {
        LambdaQueryWrapper<XunyeeBlogFavorite> qw = new LambdaQueryWrapper<>();
        qw.eq(XunyeeBlogFavorite::getVcuser_id, userId)
                .eq(XunyeeBlogFavorite::getBlog_id, blogId);
        XunyeeBlogFavorite temp = new XunyeeBlogFavorite().selectOne(qw);
        if (temp == null) {
            XunyeeBlogFavorite favorite = new XunyeeBlogFavorite();
            favorite.setVcuser_id(userId);
            favorite.setBlog_id(blogId);
            favorite.setStatus(1);
            if (favorite.insert()) {
                // 该动态添加 favorite_count 计数+1
                XunyeeBlog blog = new XunyeeBlog().selectById(blogId);
                int starCount = blog.getFavorite_count();
                blog.setFavorite_count(starCount + 1);
                blog.updateById();
                return R.OK();
            }
        } else {
            temp.setStatus(temp.getStatus() == 0 ? 1 : 0);
            if (temp.updateById()) {
                // 该动态添加 favorite_count 计数+1
                XunyeeBlog blog = new XunyeeBlog().selectById(blogId);
                int favoriteCount = blog.getFavorite_count();
                blog.setFavorite_count(temp.getStatus() == 0 ? favoriteCount - 1 : favoriteCount + 1);
                blog.updateById();
                return R.OK();
            }
        }
        return R.ERROR("收藏失败");
    }

    public R blogReport(int userId, ReqBlogReport req) {
        XunyeeBlogReport report = BeanUtil.copyProperties(req, XunyeeBlogReport.class);
        report.setVcuser_id(userId);
        if (report.insert()) {
            return R.OK();
        }
        return R.ERROR("举报失败");
    }

    public R<IPage<ResBlogPage>> getBlogByFriend(ReqMyPage myPage, int userId) {
        Page page = new Page(myPage.getCurrent(), myPage.getSize());
        IPage<ResBlogPage> iPage = myMapper.selectFriendBlogPage(page, userId);
        for (ResBlogPage record : iPage.getRecords()) {
            record.setAvatar(imageHostUtil.absImagePath(record.getAvatar()));
            if (StringUtils.isNotEmpty(record.getCover())) {
                record.setCover(imageHostUtil.absImagePath(record.getCover()));
            }
        }
        return R.OK(iPage);
    }

    public R delBlog(Integer userId, Integer blog_id) {
        LambdaUpdateWrapper<XunyeeBlog> uw=new LambdaUpdateWrapper<>();
        uw.eq(XunyeeBlog::getId,blog_id)
                .eq(XunyeeBlog::getVcuser_id,userId)
                .set(XunyeeBlog::getIs_deleted,1);
        boolean isDel=new XunyeeBlog().update(uw);
        if (isDel){
            return R.OK();
        }
        return R.ERROR();
    }
}
