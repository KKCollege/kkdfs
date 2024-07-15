package cn.kimmking.kkfs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * file meta data.
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/7/15 下午8:49
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMeta {
    private String name;
    private String originalFilename;
    private long size;
    // private String md5;
    private Map<String, String> tags = new HashMap<>();
}
