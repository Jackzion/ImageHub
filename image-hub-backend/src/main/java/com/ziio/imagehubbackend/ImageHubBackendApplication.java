package com.ziio.imagehubbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.ziio.imagehubbackend.mapper")
@EnableAspectJAutoProxy
public class ImageHubBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImageHubBackendApplication.class, args);
    }

}
