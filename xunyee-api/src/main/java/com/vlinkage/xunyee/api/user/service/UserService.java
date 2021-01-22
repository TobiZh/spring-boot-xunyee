package com.vlinkage.xunyee.api.user.service;

import cn.hutool.core.bean.BeanUtil;
import com.vlinkage.ant.xunyee.entity.XunyeeVcuser;
import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.entity.request.ReqUserInfo;
import org.springframework.stereotype.Service;

@Service
public class UserService {

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
}
