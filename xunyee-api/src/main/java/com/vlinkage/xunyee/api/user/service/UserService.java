package com.vlinkage.xunyee.api.user.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vlinkage.ant.xunyee.entity.XunyeeBlogReport;
import com.vlinkage.ant.xunyee.entity.XunyeeFollow;
import com.vlinkage.ant.xunyee.entity.XunyeeVcuser;
import com.vlinkage.ant.xunyee.entity.XunyeeVcuserReport;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.entity.request.ReqBlogReport;
import com.vlinkage.xunyee.entity.request.ReqPageFollow;
import com.vlinkage.xunyee.entity.request.ReqUserInfo;
import com.vlinkage.xunyee.entity.response.ResBlogPage;
import com.vlinkage.xunyee.entity.response.ResFollowPage;
import com.vlinkage.xunyee.mapper.MyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private MyMapper myMapper;

    public R getUser(int id) {
        XunyeeVcuser vcuser=new XunyeeVcuser().selectById(id);
        return R.OK(vcuser);
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

        // ===============  我是否以前关注过该用户  ====================
        QueryWrapper fqw=new QueryWrapper();
        fqw.eq("followed_vcuser_id",from_userid);
        fqw.eq("vcuser_id",vcuser_id);
        XunyeeFollow temp=new XunyeeFollow().selectOne(fqw);
        // ===============  我是否以前关注过该用户  ====================

        // ===============  对方是否关注了我  ====================
        QueryWrapper tqw=new QueryWrapper();
        tqw.eq("followed_vcuser_id",vcuser_id);
        tqw.eq("vcuser_id",from_userid);
        tqw.eq("status",1);
        XunyeeFollow toFollow=new XunyeeFollow().selectOne(tqw);
        // ===============  对方是否关注了我  ====================


        if (temp!=null){//如果当前用户之前关注过该用户
            int status=temp.getStatus();
            temp.setStatus(status==0?1:0);


        }else{ //当前用户并没有关注该用户
            XunyeeFollow follow=new XunyeeFollow();
            follow.setFollowed_vcuser_id(from_userid);
            follow.setVcuser_id(vcuser_id);
            follow.setStatus(1);//关注



            return R.ERROR("关注失败");
        }
        return R.ERROR("关注失败");
    }

    public R<IPage<ResFollowPage>> getFollows(Integer vcuser_id, ReqPageFollow req) {
        Page page=new Page(req.getCurrent(),req.getSize());
        IPage<ResFollowPage> iPage=myMapper.selectFollowPage(page,req.getType(),vcuser_id);
        return R.OK(iPage);
    }

    public R report(ReqBlogReport req) {
        XunyeeVcuserReport report=BeanUtil.copyProperties(req, XunyeeVcuserReport.class);
        if (report.insert()){
            return R.OK();
        }
        return R.ERROR("举报失败");
    }
}
