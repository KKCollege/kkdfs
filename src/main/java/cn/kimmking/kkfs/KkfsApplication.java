package cn.kimmking.kkfs;

import cn.kimmking.kkfs.config.KkfsConfigProperties;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static cn.kimmking.kkfs.utils.FileUtils.init;

@SpringBootApplication
@Import(RocketMQAutoConfiguration.class)
@EnableConfigurationProperties(KkfsConfigProperties.class)
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
