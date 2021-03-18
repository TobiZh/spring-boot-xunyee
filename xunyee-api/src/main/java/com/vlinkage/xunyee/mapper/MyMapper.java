package com.vlinkage.xunyee.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vlinkage.xunyee.entity.request.ReqRecommendPage;
import com.vlinkage.xunyee.entity.response.ResBlogPage;
import com.vlinkage.xunyee.entity.response.ResBrandPerson;
import com.vlinkage.xunyee.entity.response.ResFollowPage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 自定义sql的mapper
 */
@Component
public interface MyMapper {

    /**
     * 获取某个用户的创作分页
     *
     * @param page
     * @return
     */
    @Select("SELECT b.id,b.title,b.star_count,split_part(b.images,',', 1) cover,u.id vcuser_id,u.nickname,u.avatar," +
            "(SELECT CASE status WHEN 1 THEN true ELSE false END FROM xunyee_blog_star WHERE type=1 AND blog_id=b.id AND (vcuser_id=${userId})) is_star " +
            "FROM xunyee_blog b LEFT JOIN xunyee_vcuser u " +
            "ON b.vcuser_id=u.id WHERE (b.vcuser_id=${userId}) order by b.star_count desc")
    IPage<ResBlogPage> selectUserBlogPage(Page page, int userId);


    /**
     * 获取动态 无需登录
     * 动态类型 1 截屏 2 我在现场 3 品牌代言
     *
     * @param page
     * @param type
     * @return
     */
    @Select({"<script>SELECT b.id,b.title,b.star_count,split_part(b.images,',', 1) cover,u.id vcuser_id,u.nickname,u.avatar " +
            "<when test='vcuser_id!=null'>",
            ",(SELECT CASE status WHEN 1 THEN true ELSE false END FROM xunyee_blog_star WHERE type=1 AND blog_id=b.id AND (vcuser_id=${vcuser_id})) is_star ",
            "</when>",
            "FROM xunyee_blog b LEFT JOIN xunyee_vcuser u " +
                    "ON b.vcuser_id=u.id WHERE (b.type=${type}) " +
                    "ORDER BY b.star_count DESC</script>"})
    IPage<ResBlogPage> selectBlogCategoryPage(Page page, Integer type, Integer vcuser_id);

    /**
     * 获取动态 需要登录
     * 关注
     *
     * @param page
     * @param vcuser_id
     * @return
     */
    @Select("SELECT b.id,b.title,b.star_count,split_part(b.images,',', 1) cover,u.id vcuser_id,u.nickname,u.avatar, " +
            "(SELECT CASE status WHEN 1 THEN true ELSE false END FROM xunyee_blog_star WHERE type=1 AND blog_id=b.id AND (vcuser_id=${vcuser_id})) is_star " +
            "FROM xunyee_blog b LEFT JOIN xunyee_vcuser u " +
            "ON b.vcuser_id=u.id " +
            "ORDER BY b.star_count DESC")
    IPage<ResBlogPage> selectBlogFollowPage(Page page, Integer vcuser_id);


    /**
     * 搜索动态 需要登录
     * 关注
     *
     * @param page
     * @param vcuser_id
     * @return "AND (b.title like CONCAT('%',#{keyword},'%') OR b.content like CONCAT('%',#{keyword},'%'))",
     */
    @Select({"<script>SELECT b.id,b.title,b.star_count,split_part(b.images,',', 1) cover,u.id vcuser_id,u.nickname,u.avatar " +
            "<when test='vcuser_id!=null'>",
            ",(SELECT CASE status WHEN 1 THEN true ELSE false END FROM xunyee_blog_star WHERE type=1 AND blog_id=b.id AND (vcuser_id=${vcuser_id})) is_star ",
            "</when>",
            "FROM xunyee_blog b LEFT JOIN xunyee_vcuser u ",
            "ON b.vcuser_id=u.id " ,
            "<when test='name!=null'>" ,
            "WHERE b.title like CONCAT('%',#{name},'%') " ,
            "</when>",
            "ORDER BY b.star_count DESC</script>"})
    IPage<ResBlogPage> selectBlogBySearch(Page page, String name, Integer vcuser_id);


    /**
     * 获取我的关注 我的粉丝
     *
     * @param page
     * @param type      1 我的关注 2 我的粉丝
     * @param vcuser_id
     * @return
     */
    @Select({"<script>SELECT f.id,f.type,u.id vcuser_id,u.avatar,u.nickname FROM xunyee_follow f LEFT JOIN xunyee_vcuser u " +
            "<when test='type==1' >",
            " on f.followed_vcuser_id=u.id where (f.vcuser_id=${vcuser_id})",
            "</when>",
            "<when test='type==2' >",
            " on f.vcuser_id=u.id where (f.followed_vcuser_id=${vcuser_id})",
            "</when>",
            " order by f.updated desc</script>"})
    IPage<ResFollowPage> selectFollowPage(Page page, Integer type, Integer vcuser_id);


    /**
     * 获取作分页
     *
     * @param page
     * @return
     */
    @Select({"<script>SELECT b.id,b.title,b.star_count,split_part(b.images,',', 1) cover,u.id vcuser_id,u.nickname,u.avatar" +
            "<when test='vcuser_id!=null'>",
            ",(SELECT CASE status WHEN 1 THEN true ELSE false END FROM xunyee_blog_star WHERE type=1 AND blog_id=b.id AND (vcuser_id=${vcuser_id})) is_star " +
                    "</when>",
            "FROM xunyee_blog b LEFT JOIN xunyee_vcuser u " +
                    "ON b.vcuser_id=u.id WHERE (b.type=${req.type}) AND (b.person_id=${req.person_id}) AND (b.id!=${req.blog_id}) " +
                    "ORDER BY b.star_count DESC</script>"})
    IPage<ResBlogPage> selectRecommendBlogPage(Page page, Integer vcuser_id, @Param("req") ReqRecommendPage req);


    @Select("SELECT b.id,b.name,b.logo FROM brand b LEFT JOIN meta_brand_person bp ON b.id=bp.brand_id " +
            "WHERE (bp.person_id=${person_id}) ORDER BY b.created DESC")
    IPage<ResBrandPerson> selectBrandPersonPage(Page page, int person_id);

}
