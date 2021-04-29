package com.vlinkage.xunyee.api.common.service;

import com.vlinkage.xunyee.entity.result.R;
import com.vlinkage.xunyee.config.QiNiuYunConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CommonService {
    @Autowired
    private QiNiuYunConfig qiNiuYunConfig;

    public R<List<String>> qiNiuYunUploadImages(MultipartFile[] file, String pre) throws IOException {
        List<String> images = new ArrayList<>();
        for (int i = 0; i < file.length; i++) {
            String filename = file[i].getOriginalFilename();
            FileInputStream inputStream = (FileInputStream) file[i].getInputStream();
            //为文件重命名：uuid+filename
            filename = UUID.randomUUID().toString().replaceAll("-", "") + filename;
            String link = qiNiuYunConfig.uploadImgToQiNiu(inputStream, pre+filename);
            images.add(link);
        }
        return R.OK(images);
    }

    public R qiNiuYunUploadImage(MultipartFile file, String pre) throws IOException {
        String filename = file.getOriginalFilename();
        FileInputStream inputStream = (FileInputStream) file.getInputStream();
        //为文件重命名：uuid+filename
        filename = UUID.randomUUID().toString().replaceAll("-", "") + filename;
        String link = qiNiuYunConfig.uploadImgToQiNiu(inputStream, pre+filename);
        return R.OK(link);
    }
}
