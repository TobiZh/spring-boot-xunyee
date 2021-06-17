package com.vlinkage.xunyee.api.upload.controller;

import com.vlinkage.xunyee.entity.result.R;
import com.vlinkage.xunyee.api.upload.service.UploadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Api(tags = "图片上传")
@RestController
public class UploadController {


    @Autowired
    private UploadService uploadService;
    private String pre="blog/";

    @ApiOperation(value="多图上传")
    @PostMapping("/upload/images")
    public R<List<String>> qiNiuYunUploadImages(@RequestParam("file") MultipartFile[] file) throws IOException {


        return uploadService.qiNiuYunUploadImages(file,pre);
    }

    @ApiOperation(value="单张图片上传")
    @PostMapping("/upload/image")
    public R qiNiuYunUploadImage(@RequestParam("file") MultipartFile file) throws IOException {


        return uploadService.qiNiuYunUploadImage(file,pre);
    }
}
