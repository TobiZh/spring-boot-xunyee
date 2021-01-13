package com.vlinkage.xunyee.api.user.controller;

import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.api.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("test")
    public R test(LocalDate date){

        return R.OK("你好"+date);
    }

}
