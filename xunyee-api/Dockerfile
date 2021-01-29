#依赖的基础镜像jdk8
FROM openjdk:8-jre-alpine
ENV TZ=PRC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
#需要暴露出去的端口，也就是我项目的端口
EXPOSE 80
#将主机环境的jar包，以文件名demo.jar添加到docker镜像中。
ADD target/xunyee-api-1.0.0.jar xunyee-api.jar
#启动容器之后，默认的运行命令
ENTRYPOINT ["java","-jar","xunyee-api.jar"]
