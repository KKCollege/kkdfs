package cn.kimmking.kkfs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.io.File;

import static cn.kimmking.kkfs.FileUtils.init;

@SpringBootApplication
@Import(org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration.class)
public class KkfsApplication {

    public static void main(String[] args) {
        SpringApplication.run(KkfsApplication.class, args);
    }


    // 1. 基于文件存储的分布式文件系统
    // 2. 块存储   ==> 最常见，效率最高 ==> 改造成这个。
    // 3. 对象存储

    @Value("${kkfs.path}")
    private String uploadPath;

    @Bean
    ApplicationRunner runner() {
        return args -> {
            init(uploadPath);
            System.out.println("kkfs started");
        };
    }


}
