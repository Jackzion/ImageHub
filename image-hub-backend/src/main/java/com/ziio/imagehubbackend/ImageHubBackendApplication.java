package com.ziio.imagehubbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.ziio.imagehubbackend.mapper")
@EnableAspectJAutoProxy
@EnableAsync
@EnableScheduling
public class ImageHubBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImageHubBackendApplication.class, args);
    }

}
