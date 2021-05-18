package com.vlinkage.xunyee.api.vlkdj;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.vlinkage.ant.vlkdj.entity.AuthUser;
import org.springframework.stereotype.Service;

import java.util.UUID;

@DS("vlkdj")
@Service
public class VlkdjService {

    public int loginInsertUserId(int site,String openid) {
        AuthUser authUser = new AuthUser();
        authUser.setUsername(site+"_"+openid);
        authUser.setPassword(UUID.randomUUID().toString());
        authUser.setIs_staff(false);
        authUser.setIs_superuser(false);
        if (authUser.insert()){
            return authUser.getId();
        }
        return -1;
    }
}
