package com.vlinkage.xunyee.api.user.service;

import com.vlinkage.ant.xunyee.entity.XunyeeVcuser;
import com.vlinkage.common.entity.result.R;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public R getUser(int id) {
        XunyeeVcuser vcuser=new XunyeeVcuser().selectById(id);
        return R.OK(vcuser);
    }
}
