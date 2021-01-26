package com.vlinkage.xunyee.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vlinkage.ant.xunyee.entity.XunyeeFollow;
import com.vlinkage.xunyee.entity.response.ResBlogPage;
import com.vlinkage.xunyee.entity.response.ResFollowPage;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

/**
 * 自定义sql的mapper
 */
@Component
public interface MyMapper {

    /**
     * 获取某个用户的创作分页
     * @param page
     * @return
     */
    @Select("select b.id,b.title,b.star_count,split_part(b.images,',', 1) cover,u.id vcuser_id,u.nickname,u.avatar," +
            "(select CASE status WHEN 1 THEN true ELSE false END from xunyee_blog_star where type=1 and blog_id=b.id) is_star " +
            "from xunyee_blog b left join xunyee_vcuser u on b.vcuser_id=u.id where (b.vcuser_id=${userId}) order by b.star_count desc")
    IPage<ResBlogPage> selectUserBlogPage(Page page, int userId);

    /**
     * 获取动态 根据发布类型
     * @param page
     * @return
     */
    @Select("select b.id,b.title,b.star_count,split_part(b.images,',', 1) cover,u.id vcuser_id,u.nickname,u.avatar, " +
            "(select CASE status WHEN 1 THEN true ELSE false END from xunyee_blog_star where type=1 and blog_id=b.id) is_star " +
            "from xunyee_blog b left join xunyee_vcuser u on b.vcuser_id=u.id where (b.type=${type}) order by b.star_count desc")
    IPage<ResBlogPage> selectCategoryBlogPage(Page page, Integer type);


    /**
     * 获取我的关注 我的粉丝
     * @param page
     * @param type 1 我的关注 2 我的粉丝
     * @param vcuser_id
     * @return
     */
    @Select({"<secript>SELECT f.id,f.type,u.id vcuser_id,u.avatar,u.nickname FROM xunyee_follow f left join xunyee_vcuser u " +
            "<when test='type==1' >" ,
            " on f.vcuser_id=u.id",
            "</when>",
            "<when test='type==2' >" ,
            " on f.followed_vcuser_id=u.id",
            "</when>",
            "</secript>"})
    IPage<ResFollowPage> selectFollowPage(Page page, Integer type, Integer vcuser_id);
}
