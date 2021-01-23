package com.vlinkage.xunyee.api.blog.service;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vlinkage.ant.xunyee.entity.*;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.entity.ReqMyPage;
import com.vlinkage.xunyee.entity.request.ReqBlog;
import com.vlinkage.xunyee.entity.request.ReqPageBlogUser;
import com.vlinkage.xunyee.entity.response.ResBlogInfo;
import com.vlinkage.xunyee.entity.response.ResBlogPage;
import com.vlinkage.xunyee.mapper.MyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BlogService {

    @Autowired
    private MyMapper myMapper;

    public R blog(int userId, ReqBlog req) {
        XunyeeBlog xunyeeBlog=new XunyeeBlog();
        BeanUtil.copyProperties(req,xunyeeBlog);
        xunyeeBlog.setVcuserId(userId);
        if (xunyeeBlog.insert()){
            return R.OK();
        }
        return R.ERROR("发布失败");
    }

    public R<IPage<ResBlogPage>> getBlogByUserId(ReqPageBlogUser req) {
        Page page=new Page(req.getCurrent(),req.getSize());
        IPage<ResBlogPage> iPage=myMapper.selectUserBlogPage(page,req.getVcuserId());
        return R.OK(iPage);
    }

    public R<IPage<ResBlogPage>> blogCategory(ReqMyPage myPage,Integer type) {
        Page page=new Page(myPage.getCurrent(),myPage.getSize());
        IPage<ResBlogPage> iPage=myMapper.selectCategoryBlogPage(page,type);
        return R.OK(iPage);
    }

    public R<ResBlogInfo> blogInfo(Integer userId, Integer blogId) {
        ResBlogInfo info=new ResBlogInfo();

        // 动态信息
        XunyeeBlog blog=new XunyeeBlog().selectById(blogId);
        info.setBlogId(blog.getId());
        info.setTitle(blog.getTitle());
        info.setContent(blog.getContent());
        info.setImages(blog.getImages());
        info.setCreated(blog.getCreated());
        info.setType(blog.getType());
        info.setTypeId(blog.getTypeId());
        info.setPersonId(blog.getPersonId());

        info.setStarCount(blog.getStarCount());
        info.setUnstarCount(blog.getUnstarCount());
        info.setFavoriteCount(blog.getFavoriteCount());

        // 用户信息
        XunyeeVcuser vcuser=new XunyeeVcuser().selectById(blog.getVcuserId());
        info.setVcuserId(vcuser.getId());
        info.setNickname(vcuser.getNickname());
        info.setAvatar(vcuser.getAvatar());
        // 相关艺人

        // 如果是品牌 显示相关品牌

        // 点赞 点踩
        QueryWrapper sqw=new QueryWrapper();
        sqw.eq("vcuser_id",userId);
        sqw.eq("blog_id",blogId);
        sqw.eq("status",1);
        int isStar=new XunyeeBlogStar().selectCount(sqw);
        sqw.eq("type",0);
        int isUnStar=new XunyeeBlogStar().selectCount(sqw);
        info.setIsStar(isStar==0?false:true);
        info.setIsUnstar(isUnStar==0?false:true);

        //收藏 关注
        QueryWrapper fqw=new QueryWrapper();
        fqw.eq("vcuser_id",userId);
        fqw.eq("blog_id",blogId);
        sqw.eq("status",1);
        int isFavorite=new XunyeeBlogFavorite().selectCount(fqw);
        info.setIsFavorite(isFavorite==0?false:true);

        return R.OK(info);
    }

    @Transactional
    public R blogStar(Integer userId, int blogId, int type) {
        QueryWrapper qw=new QueryWrapper();
        qw.eq("vcuser_id",userId);
        qw.eq("blog_id",blogId);
        qw.eq("type",type);
        XunyeeBlogStar temp=new XunyeeBlogStar().selectOne(qw);
        if (temp==null){
            XunyeeBlogStar blogSatr=new XunyeeBlogStar();
            blogSatr.setVcuserId(userId);
            blogSatr.setBlogId(blogId);
            blogSatr.setType(type);
            blogSatr.setStatus(1);
            if (blogSatr.insert()){
                // 该动态添加 star_count计数+1
                XunyeeBlog blog=new XunyeeBlog().selectById(blogId);
                if (type==0){
                    //点踩
                    blog.setUnstarCount(blog.getUnstarCount()+1);
                }else if (type==1){
                    //点赞
                    blog.setStarCount(blog.getStarCount()+1);
                }
                blog.updateById();
                return R.OK();
            }
        }else{
            temp.setStatus(temp.getStatus()==0?1:0);
            if (temp.updateById()) {
                // 该动态添加 star_count计数+1
                XunyeeBlog blog=new XunyeeBlog().selectById(blogId);
                if (type==0){
                    //点踩
                    int unstarCount=blog.getUnstarCount();
                    blog.setUnstarCount(temp.getStatus()==0?unstarCount-1:unstarCount+1);
                }else if (type==1){
                    //点赞
                    int starCount=blog.getStarCount();
                    blog.setStarCount(temp.getStatus()==0?starCount-1:starCount+1);
                }
                blog.setStarCount(blog.getStarCount()+1);
                blog.updateById();
                return R.OK();
            }
        }
        return R.ERROR(type==0?"点踩失败":"点赞失败");
    }

    public R blogFavorite(Integer userId, int blogId) {
        QueryWrapper qw=new QueryWrapper();
        qw.eq("vcuser_id",userId);
        qw.eq("blog_id",blogId);
        XunyeeBlogFavorite temp=new XunyeeBlogFavorite().selectOne(qw);
        if (temp==null){
            XunyeeBlogFavorite favorite=new XunyeeBlogFavorite();
            favorite.setVcuserId(userId);
            favorite.setBlogId(blogId);
            favorite.setStatus(1);
            if (favorite.insert()){
                // 该动态添加 favorite_count 计数+1
                XunyeeBlog blog=new XunyeeBlog().selectById(blogId);
                int starCount=blog.getFavoriteCount();
                blog.setFavoriteCount(starCount+1);
                blog.updateById();
                return R.OK();
            }
        }else{
            temp.setStatus(temp.getStatus()==0?1:0);
            if (temp.updateById()) {
                // 该动态添加 favorite_count 计数+1
                XunyeeBlog blog=new XunyeeBlog().selectById(blogId);
                int favoriteCount=blog.getFavoriteCount();
                blog.setFavoriteCount(temp.getStatus()==0?favoriteCount-1:favoriteCount+1);
                blog.updateById();
                return R.OK();
            }
        }
        return R.ERROR("收藏失败");
    }
}
