package com.mall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.mall.gateway", "com.mall.common"})
public class GatewayApplication {
    /*
        启动 Spring Boot 容器，触发组件扫描，把你写的 @Component、@Configuration 都注册成 Bean
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
