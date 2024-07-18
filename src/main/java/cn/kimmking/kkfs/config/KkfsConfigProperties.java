package cn.kimmking.kkfs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * config properties.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/7/18 下午3:42
 */

@ConfigurationProperties(prefix = "kkfs")
@Data
public class KkfsConfigProperties {
    private String uploadPath;
    private String backupUrl;
    private String downloadUrl;
    private String group;
    private boolean autoMd5;
    private boolean syncBackup;
}
