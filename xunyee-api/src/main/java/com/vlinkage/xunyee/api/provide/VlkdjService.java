package com.vlinkage.xunyee.api.provide;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vlinkage.ant.vlkdj.entity.AuthUser;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@DS("vlkdj")
@Service
public class VlkdjService {

    public Integer loginInsertUserId(int site,String openid) {
        AuthUser authUser = new AuthUser();
        authUser.setUsername(site+"_"+openid);
        authUser.setPassword(UUID.randomUUID().toString());
        authUser.setFirst_name("");
        authUser.setLast_name("");
        authUser.setEmail("");
        authUser.setIs_staff(false);
        authUser.setIs_superuser(false);
        authUser.setIs_active(true);
        Date nowDate=new Date();
        authUser.setLast_login(nowDate);
        authUser.setDate_joined(nowDate);
        if (authUser.insert()){
            return authUser.getId();
        }
        return -1;
    }

    public List<AuthUser> getUser() {
        QueryWrapper qw=new QueryWrapper();
        qw.last("limit 20");
        List<AuthUser> authUser = new AuthUser().selectList(qw);

        return authUser;

    }
}
