package com.vlinkage.xunyee.api.user.controller;

import com.vlinkage.ant.xunyee.entity.XunyeeAd;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.user.service.UserService;
import com.vlinkage.xunyee.entity.request.ReqAd;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Api(tags = "用户模块")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("test")
    public R test(@Valid ReqAd reqAd){


        List<XunyeeAd> ad=new XunyeeAd().selectAll();

        return R.OK(ad);
    }

}
