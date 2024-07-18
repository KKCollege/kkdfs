package cn.kimmking.kkfs.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * config file temp dir.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/7/10 下午9:10
 */
@Configuration
public class KkfsConfig {

    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setLocation("/private/tmp/tomcat");
        return factory.createMultipartConfig();
    }

}
