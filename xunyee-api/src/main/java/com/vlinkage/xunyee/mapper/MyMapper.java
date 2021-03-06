package com.vlinkage.xunyee.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vlinkage.xunyee.entity.request.ReqRecommendPage;
import com.vlinkage.xunyee.entity.response.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 自定义sql的mapper
 */
@Component
public interface MyMapper {

    /**
     * 获取某个用户的动态
     *
     * @param page
     * @return
     */
    @Select({"<script>SELECT b.id,b.title,SUBSTRING(b.content,1,10) \"content\",b.star_count,split_part(b.images,',', 1) cover,u.id vcuser_id,u.nickname,u.avatar" +
            "<when test='from_user_id!=null'>",
            ",(SELECT CASE status WHEN 1 THEN true ELSE false END FROM xunyee_blog_star WHERE type=1 AND blog_id=b.id AND vcuser_id=#{from_user_id}) is_star ",
            "</when>",
            "FROM xunyee_blog b LEFT JOIN xunyee_vcuser u ",
            "ON b.vcuser_id=u.id ",
            "WHERE b.is_deleted=false AND b.vcuser_id=#{vcuser_id} order by b.star_count desc</script>"})
    IPage<ResBlogPage> selectUserBlogPage(Page page, int vcuser_id,Integer from_user_id);


    /**
     * 获取动态 无需登录
     * 动态类型 1 截屏 2 我在现场 3 品牌代言
     *
     * @param page
     * @param type
     * @return
     */
    @Select({"<script>SELECT b.id,b.title,SUBSTRING(b.content,1,10) \"content\",b.star_count,split_part(b.images,',', 1) cover,u.id vcuser_id,u.nickname,u.avatar " +
            "<when test='vcuser_id!=null'>",
            ",(SELECT CASE status WHEN 1 THEN true ELSE false END FROM xunyee_blog_star WHERE type=1 AND blog_id=b.id AND vcuser_id=#{vcuser_id}) is_star ",
            "</when>",
            "FROM xunyee_blog b LEFT JOIN xunyee_vcuser u ",
            "ON b.vcuser_id=u.id ",
            "WHERE b.is_deleted=false AND b.type=#{type} ",
            "ORDER BY b.star_count DESC</script>"})
    IPage<ResBlogPage> selectBlogCategoryPage(Page page, Integer type, Integer vcuser_id);


    /**
     * 获取动态 无需登录
     * 推荐
     * @param page
     * @return
     */
    @Select({"<script>SELECT b.id,b.title,SUBSTRING(b.content,1,10) \"content\",b.star_count,split_part(b.images,',', 1) cover,u.id vcuser_id,u.nickname,u.avatar " +
            "<when test='vcuser_id!=null'>",
            ",(SELECT CASE status WHEN 1 THEN true ELSE false END FROM xunyee_blog_star WHERE type=1 AND blog_id=b.id AND vcuser_id=#{vcuser_id}) is_star ",
            "</when>",
            "FROM xunyee_blog b LEFT JOIN xunyee_vcuser u ",
            "ON b.vcuser_id=u.id ",
            "WHERE b.is_deleted=false ",
            "ORDER BY b.star_count DESC</script>"})
    IPage<ResBlogPage> selectBlogRecommendPage(Page page, Integer userId);

    /**
     * 获取动态 需要登录
     * 关注
     *
     * @param page
     * @param vcuser_id
     * @return
     */
    @Select("SELECT b.id,b.title,SUBSTRING(b.content,1,10) \"content\",b.star_count,split_part(b.images,',', 1) cover,u.id vcuser_id,u.nickname,u.avatar, " +
            "(SELECT CASE status WHEN 1 THEN true ELSE false END FROM xunyee_blog_star WHERE type=1 AND blog_id=b.id AND vcuser_id=#{vcuser_id}) is_star " +
            "FROM xunyee_blog b, xunyee_follow f, xunyee_vcuser u " +
            "where b.vcuser_id=f.followed_vcuser_id and f.vcuser_id=#{vcuser_id} and b.vcuser_id=u.id AND b.is_deleted=false " +
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
    @Select({"<script>SELECT b.id,b.title,SUBSTRING(b.content,1,10) \"content\",b.star_count,split_part(b.images,',', 1) cover,u.id vcuser_id,u.nickname,u.avatar " +
            "<when test='vcuser_id!=null'>",
            ",(SELECT CASE status WHEN 1 THEN true ELSE false END FROM xunyee_blog_star WHERE type=1 AND blog_id=b.id AND vcuser_id=#{vcuser_id}) is_star ",
            "</when>",
            "FROM xunyee_blog b LEFT JOIN xunyee_vcuser u ",
            "ON b.vcuser_id=u.id AND b.is_deleted=false " ,
            "<when test='name!=null and name!=\"\"'>" ,
            "WHERE b.title like CONCAT('%',#{name}::text,'%') or b.content like CONCAT('%',#{name}::text,'%') " ,
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
    @Select({"<script>SELECT f.id,f.type,u.id vcuser_id,u.bio,u.avatar,u.nickname FROM xunyee_follow f,xunyee_vcuser u " +
            "WHERE f.status=1 " +
            "<when test='type==1' >",
            "AND f.followed_vcuser_id=u.id AND f.vcuser_id=#{vcuser_id} ",
            "</when>",
            "<when test='type==2' >",
            "AND f.vcuser_id=u.id AND f.followed_vcuser_id=#{vcuser_id} ",
            "</when>",
            "ORDER BY f.updated DESC</script>"})
    IPage<ResFollowPage> selectFollowPage(Page page, Integer type, Integer vcuser_id);


    /**
     * 获取动态分页
     *
     * @param page
     * @return
     */
    @Select({"<script>SELECT b.id,b.title,SUBSTRING(b.content,1,10) \"content\",b.star_count,split_part(b.images,',', 1) cover,u.id vcuser_id,u.nickname,u.avatar" +
            "<when test='vcuser_id!=null'>",
            ",(SELECT CASE status WHEN 1 THEN true ELSE false END FROM xunyee_blog_star WHERE type=1 AND blog_id=b.id AND vcuser_id=#{vcuser_id}) is_star ",
            "</when>",
            "FROM xunyee_blog b LEFT JOIN xunyee_vcuser u ",
            "ON b.vcuser_id=u.id ",
            "WHERE b.is_deleted=false AND b.type=#{req.type} AND b.person_id=#{req.person_id} AND b.id!=#{req.blog_id} ",
            "ORDER BY b.star_count DESC</script>"})
    IPage<ResBlogPage> selectRecommendBlogPage(Page page, Integer vcuser_id, @Param("req") ReqRecommendPage req);


    @Select("SELECT b.id,b.name,b.logo,bps.url_gen url,bps.finish_time_new FROM brand b " +
            "LEFT JOIN meta_brand_person bp ON b.id=bp.brand_id and bp.person_id=#{person_id} " +
            "LEFT JOIN meta_brand_person_site bps ON bp.id=bps.brand_person_id " +
            "WHERE bps.is_enabled=true AND bps.url<>'' ORDER BY bps.finish_time_new DESC,bps.created DESC")
    List<ResBrandPersonList> selectBrandPersonList(int person_id);

    @Select("SELECT b.id,b.name,bps.url_gen url "+
            "FROM brand b " +
            "LEFT JOIN meta_brand_person bp ON b.id=bp.brand_id and bp.person_id=#{person_id} and bp.brand_id=#{brand_id} " +
            "LEFT JOIN meta_brand_person_site bps ON bp.id=bps.brand_person_id " +
            "WHERE bps.is_enabled=true AND bps.url<>'' ")
    ResBrandNameUrl selectBrandNameUrlById(int brand_id,int person_id);


    /**
     * 获取好友动态
     * 互相关注的是好友
     * @param page
     * @param vcuser_id
     * @return
     */
    @Select("SELECT b.id,b.title,SUBSTRING(b.content,1,10) \"content\",b.star_count,split_part(b.images,',', 1) cover,u.id vcuser_id,u.nickname,u.avatar," +
            "(SELECT CASE status WHEN 1 THEN true ELSE false END FROM xunyee_blog_star WHERE type=1 AND blog_id=b.id AND vcuser_id=#{vcuser_id}) is_star " +
            "FROM xunyee_blog b " +
            "LEFT JOIN xunyee_follow f ON b.vcuser_id=f.followed_vcuser_id " +
            "LEFT JOIN xunyee_vcuser u ON b.vcuser_id=u.id " +
            "WHERE f.type=3 AND f.status=1 AND f.vcuser_id=#{vcuser_id}")
    IPage<ResBlogPage> selectFriendBlogPage(Page page, int vcuser_id);



    /**
     * 搜索动态 需要登录
     * 关注
     *
     * @param page
     * @param vcuser_id
     * @return "AND (b.title like CONCAT('%',#{keyword},'%') OR b.content like CONCAT('%',#{keyword},'%'))",
     */
    @Select({"<script>SELECT b.id,b.title,SUBSTRING(b.content,1,10) \"content\",b.star_count,split_part(b.images,',', 1) cover,u.id vcuser_id,u.nickname,u.avatar " +
            "<when test='vcuser_id!=null'>",
            ",(SELECT CASE status WHEN 1 THEN true ELSE false END FROM xunyee_blog_star WHERE type=1 AND blog_id=b.id AND vcuser_id=#{vcuser_id}) is_star ",
            "</when>",
            "FROM xunyee_blog b LEFT JOIN xunyee_vcuser u ",
            "ON b.vcuser_id=u.id WHERE b.is_deleted=false AND b.vcuser_id=#{vcuser_id}" ,
            "<when test='name!=null and name!=\"\"'>" ,
            " AND b.title like CONCAT('%',#{name}::text,'%') " ,
            "</when>",
            "ORDER BY b.star_count DESC</script>"})
    IPage<ResBlogPage> selectMineBlogPage(Page page, int vcuser_id, String name);



    @Select("select b.id blog_id,u.id vcuser_id,u.avatar,u.nickname,s.updated created,SUBSTRING(b.content,1,20) \"content\",b.images," +
            "case when (select count(*) from xunyee_vcuser_benefit where now()<=finish_time and vcuser_id=#{vcuser_id})>0 " +
            "then true else false end is_vip " +
            "FROM xunyee_blog_star s,xunyee_blog b,xunyee_vcuser u " +
            "where  b.is_deleted=false and s.blog_id=b.id and s.\"type\"=1 and s.status=1 " +
            "and s.vcuser_id=u.id and b.vcuser_id=#{vcuser_id} " +
            "order by s.updated desc")
    IPage<ResBlogStarPage> selectBlogStarPage(Page page, int vcuser_id);

    @Select("select b.id,u.id vcuser_id,u.avatar,u.nickname,s.created,split_part(b.images,',', 1) cover,true is_star,b.star_count " +
            "FROM xunyee_blog_star s,xunyee_blog b,xunyee_vcuser u " +
            "where b.is_deleted=false and s.blog_id=b.id and s.status=1 and b.vcuser_id=u.id and s.vcuser_id=#{vcuser_id}")
    IPage<ResMyBlogStarPage> selectMyBlogStarPage(Page page, int vcuser_id);

    @Select("select b.id,u.id vcuser_id,u.avatar,u.nickname,s.created,split_part(b.images,',', 1) cover,b.star_count," +
            "(SELECT CASE status WHEN 1 THEN true ELSE false END FROM xunyee_blog_star WHERE type=1 AND blog_id=b.id AND vcuser_id=#{vcuser_id}) is_star "+
            "FROM xunyee_blog_favorite s,xunyee_blog b,xunyee_vcuser u " +
            "where b.is_deleted=false and s.blog_id=b.id and s.status=1 and b.vcuser_id=u.id and s.vcuser_id=#{vcuser_id}")
    IPage<ResMyBlogStarPage> selectMyBlogFavoritePage(Page page, int vcuser_id);

    @Select("select b.id,u.id vcuser_id,u.avatar,u.nickname,s.created,split_part(b.images,',', 1) cover,b.star_count," +
            "(SELECT CASE status WHEN 1 THEN true ELSE false END FROM xunyee_blog_star WHERE type=1 AND blog_id=b.id AND vcuser_id=#{vcuser_id}) is_star "+
            "FROM xunyee_blog_browsing_history s,xunyee_blog b,xunyee_vcuser u " +
            "where b.is_deleted=false and s.blog_id=b.id and b.vcuser_id=u.id and s.vcuser_id=#{vcuser_id}")
    IPage<ResMyBlogStarPage> selectMyBlogBrowHistoryPage(Page page, int vcuser_id);



//    SELECT
//    xbar.x_star,
//    xbar.x_unstar,
//    xbar_user.nickname,
//    b.*
//    FROM
//    xunyee_blog b,
//	(
//    SELECT ID,
//    round( ( star_count - 0 ) / ( 1000 - 0 ) :: NUMERIC, 4 ) AS x_star,
//    round( ( unstar_count - 0 ) / ( 1000 - 0 )*0.3 :: NUMERIC, 4 ) AS x_unstar,
//    round( ( favorite_count - 0 ) / ( 1000 - 0 ) :: NUMERIC, 4 ) AS x_favorite,
//    round( ( report_count - 0 ) / ( 1000 - 0 )*0.3 :: NUMERIC, 4 ) AS x_report
//    FROM xunyee_blog ) xbar,
//            (select * from xunyee_vcuser) xbar_user
//            WHERE
//    b.ID = xbar.ID
//    and b.vcuser_id=xbar_user.id
//    ORDER BY
//    xbar.x_star DESC
}
