package com.vlinkage.xunyee.api.blog.service;


import cn.hutool.core.bean.BeanUtil;
import com.vlinkage.ant.xunyee.entity.XunyeeBlog;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.entity.request.ReqBlog;
import org.springframework.stereotype.Service;

@Service
public class BlogService {

    public R blog(int userId, ReqBlog req) {
        XunyeeBlog xunyeeBlog=new XunyeeBlog();
        BeanUtil.copyProperties(req,xunyeeBlog);
        xunyeeBlog.setVcuser_id(userId);
        if (xunyeeBlog.insert()){
            return R.OK();
        }
        return R.ERROR("发布失败");
    }
}
