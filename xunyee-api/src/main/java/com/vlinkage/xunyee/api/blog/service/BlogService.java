package com.vlinkage.xunyee.api.blog.service;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vlinkage.ant.xunyee.entity.XunyeeBlog;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.entity.ReqMyPage;
import com.vlinkage.xunyee.entity.request.ReqBlog;
import com.vlinkage.xunyee.entity.request.ReqPageBlogUser;
import com.vlinkage.xunyee.entity.response.ResBlogPage;
import com.vlinkage.xunyee.mapper.MyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BlogService {

    @Autowired
    private MyMapper myMapper;

    public R blog(int userId, ReqBlog req) {
        XunyeeBlog xunyeeBlog=new XunyeeBlog();
        BeanUtil.copyProperties(req,xunyeeBlog);
        xunyeeBlog.setVcuser_id(userId);
        if (xunyeeBlog.insert()){
            return R.OK();
        }
        return R.ERROR("发布失败");
    }

    public R<IPage<ResBlogPage>> getBlogByUserId(ReqPageBlogUser req) {
        Page page=new Page(req.getCurrent(),req.getSize());
        IPage<ResBlogPage> iPage=myMapper.selectGoodDeedPage(page,req.getVcuser_id());
        return R.OK(iPage);
    }
}
