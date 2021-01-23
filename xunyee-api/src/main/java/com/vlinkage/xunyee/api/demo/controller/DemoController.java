package com.vlinkage.xunyee.api.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vlinkage.ant.xunyee.entity.XunyeeAd;
import com.vlinkage.ant.xunyee.entity.XunyeeBlog;
import com.vlinkage.ant.xunyee.entity.XunyeeVcuser;
import com.vlinkage.ant.xunyee.entity.XunyeeVcuserBenefit;
import com.vlinkage.common.entity.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "示例代码")
@RestController
public class DemoController {


    @ApiOperation("时间")
    @GetMapping("benefits/50")
    public R<List<XunyeeVcuser>> benefits(){
        QueryWrapper qw=new QueryWrapper();
        qw.last("limit 50");
        List<XunyeeVcuserBenefit> benefits=new XunyeeVcuserBenefit().selectList(qw);
        return R.OK(benefits);
    }

    @ApiOperation("一次查询指定数量")
    @GetMapping("test/50")
    public R<List<XunyeeVcuser>> test(){
        QueryWrapper qw=new QueryWrapper();
        qw.last("limit 50");
        List<XunyeeVcuser> vcusers=new XunyeeVcuser().selectList(qw);
        return R.OK(vcusers);
    }

    @ApiOperation("分页")
    @GetMapping("page")
    public R<IPage<XunyeeVcuser>> test(Page page,XunyeeVcuser vcuser){
        QueryWrapper qw=new QueryWrapper();
        if (!StringUtils.isEmpty(vcuser.getNickname())){
            qw.like("nickname",vcuser.getNickname());
        }
        IPage<XunyeeVcuser> vcusers=new XunyeeVcuser().selectPage(page,qw);
        return R.OK(vcusers);
    }


    @ApiOperation("ad")
    @GetMapping("ad")
    public R<List<XunyeeVcuser>> ad(){
        List<XunyeeAd> ads=new XunyeeAd().selectAll();
        return R.OK(ads);
    }

    @ApiOperation("blog")
    @GetMapping("blog")
    public R<List<XunyeeBlog>> blog(){
        List<XunyeeBlog> ads=new XunyeeBlog().selectAll();
        return R.OK(ads);
    }

    @ApiOperation("blog")
    @PostMapping("blog")
    public R insertblog(XunyeeBlog blog){
        if (blog.insert()){
            return R.OK(blog);
        }
        return R.ERROR();
    }
}
