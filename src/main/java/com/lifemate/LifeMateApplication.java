package com.lifemate;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.lifemate.mapper")
@SpringBootApplication
public class LifeMateApplication {
    //
    public static void main(String[] args) {
        SpringApplication.run(LifeMateApplication.class, args);
    }

}
