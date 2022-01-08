
package com.common.msg.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAutoConfiguration
@EnableScheduling
@EnableAsync

@ComponentScan(basePackages = {"com.common.msg"})
public class KafkaApplication {

    public static void main(String[] args) {
        // 本地运行时需配置系统环境变量
        //  System.setProperty("PROJECT_ID", "95208");
        System.setProperty("DEPLOY_ENV", "test");
        SpringApplication.run(KafkaApplication.class, args);
    }

}
