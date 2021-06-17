#!/bin/bash
image_name="registry.cn-shanghai.aliyuncs.com/xunyee/springboot-xunyee-api"
echo "删除镜像：${image_name}"
docker rmi ${image_name}
echo "开始构建"
docker build -t ${image_name} .
echo "推送到阿里云"
docker push ${image_name}
echo "完成"