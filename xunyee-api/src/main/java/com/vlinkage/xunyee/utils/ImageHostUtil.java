package com.vlinkage.xunyee.utils;

import com.vlinkage.xunyee.config.xunyee.XunyeeProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ImageHostUtil {

   @Autowired
   private XunyeeProperties xunyeeProperties;

    public String absImagePath(String imgUrl){
        if (StringUtils.isNotEmpty(imgUrl)&&!StringUtils.startsWith(imgUrl,"http")){
            imgUrl=xunyeeProperties.getImageHost()+imgUrl;
        }
        return imgUrl;
    }

    public String removeImagePath(String imgUrl){
        if (StringUtils.startsWith(imgUrl,xunyeeProperties.getImageHost())){
            imgUrl=imgUrl.replace(xunyeeProperties.getImageHost(),"");
        }
        return imgUrl;
    }
}
