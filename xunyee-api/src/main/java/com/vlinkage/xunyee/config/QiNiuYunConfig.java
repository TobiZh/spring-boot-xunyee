package com.vlinkage.xunyee.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.FileInputStream;

@Component
public class QiNiuYunConfig {

//    private String accessKey="beKqOPRQNzaiCbz1PERh9UPQtLSce3haz1S65qAf";
//    private String secretKey="NQ-IoPLVYq6xDGATvvP0Wt4XRMX2j9CTXVkiYwnW";
//    private String bucket="tobi";

    @Resource
    private QiNiuProperties qiNiuProperties;
    @Autowired
    private ObjectMapper objectMapper;

    public String uploadImgToQiNiu(FileInputStream file, String filename) {
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.region0());
        // 其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        // 生成密钥
        Auth auth = Auth.create(qiNiuProperties.getAccessKey(), qiNiuProperties.getSecretKey());
        try {
            String upToken = auth.uploadToken(qiNiuProperties.getBucket());
            try {
                Response response = uploadManager.put(file, filename, upToken, null, null);
                // 解析上传成功的结果
                DefaultPutRet putRet = objectMapper.readValue(response.bodyString(), DefaultPutRet.class);
                // 这个returnPath是获得到的外链地址,通过这个地址可以直接打开图片
                String returnPath = putRet.key;
                return returnPath;
            } catch (QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                try {
                    System.err.println(r.bodyString());
                } catch (QiniuException ex2) {
                    //ignore
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}

