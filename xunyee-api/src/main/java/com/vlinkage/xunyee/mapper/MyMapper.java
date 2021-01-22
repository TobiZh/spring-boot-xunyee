package com.vlinkage.xunyee.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vlinkage.xunyee.entity.response.ResBlogPage;
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
    @Select("select b.id,b.title,b.star_count,split_part(b.images,',', 1) cover,u.id vcuser_id,u.nickname,u.avatar " +
            "from xunyee_blog b left join xunyee_vcuser u on b.vcuser_id=u.id where (b.vcuser_id=${userId}) order by b.star_count desc")
    IPage<ResBlogPage> selectUserBlogPage(Page page, int userId);

    /**
     * 获取动态根据发布类型
     * @param page
     * @return
     */
    @Select("select b.id,b.title,b.star_count,split_part(b.images,',', 1) cover,u.id vcuser_id,u.nickname,u.avatar " +
            "from xunyee_blog b left join xunyee_vcuser u on b.vcuser_id=u.id where (b.type=${type}) order by b.star_count desc")
    IPage<ResBlogPage> selectCategoryBlogPage(Page page, Integer type);
}
