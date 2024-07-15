package cn.kimmking.kkfs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * file meta data.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/7/15 下午7:08
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMeta {
    private String name;
    private String originName;
    private String md5;
    private long size;
    private String type;
    private Map<String,String> tags = new HashMap<>();
}
