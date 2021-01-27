package com.vlinkage.xunyee.api.xunyee.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vlinkage.ant.xunyee.entity.*;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.entity.ReqMyPage;
import com.vlinkage.xunyee.entity.request.ReqFeedback;
import com.vlinkage.xunyee.entity.request.ReqPic;
import com.vlinkage.xunyee.entity.response.ResNavigation;
import com.vlinkage.xunyee.entity.response.ResPic;
import com.vlinkage.xunyee.entity.response.ResSearchHot;
import com.vlinkage.xunyee.entity.response.ResSystemNotification;
import com.vlinkage.xunyee.utils.CopyListUtil;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class XunyeeService {

    public R<List<ResPic>> getPic(ReqPic req) {
        LocalDateTime nowDate=LocalDateTime.now();

        QueryWrapper qw=new QueryWrapper();
        qw.eq("type_id",req.getType_id());
        qw.eq("is_enabled_5",req.getIs_enabled_mini());
        qw.eq("is_enabled_6",req.getIs_enabled_app());
        qw.orderByAsc("sequence");
        qw.le("start_time",nowDate);// >=
        qw.ge("finish_time",nowDate);// <=
        List<XunyeePic> xunyeePics=new XunyeePic().selectList(qw);
        List<ResPic> resPics=CopyListUtil.copyListProperties(xunyeePics, ResPic.class);
        return R.OK(resPics);
    }

    public R<List<ResNavigation>> getNavigation() {
        QueryWrapper qw=new QueryWrapper();
        qw.orderByAsc("orderby");
        qw.eq("is_deleted",0);
        qw.eq("is_enabled",1);
        List<XunyeeNavigation> navigation=new XunyeeNavigation().selectList(qw);
        List<ResNavigation> resNavigations=CopyListUtil.copyListProperties(navigation,ResNavigation.class);
        return R.OK(resNavigations);
    }

    public R<List<ResSearchHot>> getSearchHot() {
        QueryWrapper qw=new QueryWrapper();
        qw.eq("is_deleted",false);
        qw.orderByAsc("orderby");
        qw.select("id","name");
        List<XunyeeSearchHot> searchHot=new XunyeeSearchHot().selectList(qw);
        List<ResSearchHot> resSearchHots=CopyListUtil.copyListProperties(searchHot,ResSearchHot.class);
        return R.OK(resSearchHots);
    }

    public R feedback(int userId, ReqFeedback req) {
        XunyeeFeedback feedback= BeanUtil.copyProperties(req,XunyeeFeedback.class);
        feedback.setVcuser_id(userId);
        if (feedback.insert()){
            return R.OK();
        }
        return R.ERROR();
    }

    public R<IPage<ResSystemNotification>> systemNotification(int userId, ReqMyPage myPage) {

        QueryWrapper qw=new QueryWrapper();
        qw.eq("receive_vcuser_id",userId);
        qw.eq("receive_vcuser_id",0);//所有人都能收到的
        Page page=new Page(myPage.getCurrent(),myPage.getSize());
        IPage<ResSystemNotification> iPage=new XunyeeSystemNotification().selectPage(page,qw);
        iPage.setRecords(CopyListUtil.copyListProperties(iPage.getRecords(), ResSystemNotification.class));

        return R.OK(iPage);
    }

    public R systemNotificationRead(int id) {
        XunyeeSystemNotification notification=new XunyeeSystemNotification().selectById(id);
        if (notification!=null){
            if (notification.getIs_read()){
                return R.ERROR("已标记过");
            }
            notification.setIs_read(true);
            notification.setRead_time(new Date());
            if (notification.updateById()){
                return R.OK();
            }
            return R.ERROR();
        }
        return R.ERROR("该通知不存在");
    }
}
