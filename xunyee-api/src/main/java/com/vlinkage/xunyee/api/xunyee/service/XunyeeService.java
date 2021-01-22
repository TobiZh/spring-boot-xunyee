package com.vlinkage.xunyee.api.xunyee.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vlinkage.ant.xunyee.entity.XunyeePic;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.entity.request.ReqPic;
import com.vlinkage.xunyee.entity.response.ResPic;
import com.vlinkage.xunyee.utils.CopyListUtil;
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
        qw.eq("is_enabled_5",req.getIs_enabled_5());
        qw.eq("is_enabled_6",req.getIs_enabled_6());
        qw.orderByAsc("sequence");
        qw.le("start_time",nowDate);// >=
        qw.ge("finish_time",nowDate);// <=
        List<XunyeePic> xunyeePics=new XunyeePic().selectList(qw);
        List<ResPic> resPics=CopyListUtil.copyListProperties(xunyeePics, ResPic.class);
        return R.OK(resPics);
    }
}