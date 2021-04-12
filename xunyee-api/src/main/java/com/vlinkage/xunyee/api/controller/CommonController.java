package com.vlinkage.xunyee.api.controller;

import com.vlinkage.common.entity.result.R;
import com.vlinkage.xunyee.config.QiNiuYunConfig;
import com.vlinkage.xunyee.jwt.PassToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Api(tags = "公共接口")
@RestController
public class CommonController {

    @Autowired
    private QiNiuYunConfig qiNiuYunConfig;


    @ApiOperation(value="多图上传")
    @PassToken
    @PostMapping("/upload/images")
    public R qiNiuYunUploadImages(@RequestParam("file") MultipartFile[] file) throws IOException {
        List<String> images = new ArrayList<>();
        for (int i = 0; i < file.length; i++) {
            String filename = file[i].getOriginalFilename();
            FileInputStream inputStream = (FileInputStream) file[i].getInputStream();
            //为文件重命名：uuid+filename
            filename = UUID.randomUUID().toString().replaceAll("-", "") + filename;
            String link = qiNiuYunConfig.uploadImgToQiNiu(inputStream, filename);
            images.add(link);
        }
        return R.OK(images);
    }

    @ApiOperation(value="单张图片上传")
    @PassToken
    @PostMapping("/upload/image")
    public R qiNiuYunUploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        FileInputStream inputStream = (FileInputStream) file.getInputStream();
        //为文件重命名：uuid+filename
        filename = UUID.randomUUID().toString().replaceAll("-", "") + filename;
        String link = qiNiuYunConfig.uploadImgToQiNiu(inputStream, filename);
        return R.OK(link);
    }
}
