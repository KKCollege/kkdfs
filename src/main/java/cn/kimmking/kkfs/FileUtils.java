package cn.kimmking.kkfs;

import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

/**
 * Utils for file.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/7/15 下午8:12
 */
public class FileUtils {

    static String DEFAULT_MIME_TYPE = "application/octet-stream";

    public static String getMimeType(String fileName) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String content = fileNameMap.getContentTypeFor(fileName);
        return content == null ? DEFAULT_MIME_TYPE : content;
    }

    public static void init(String uploadPath) {
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        for (int i = 0; i < 256; i++) {
            String subdir = String.format("%02x", i);
            File file = new File(uploadPath + "/" + subdir);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
    }

    public static String getUUIDFile(String file) {
        return UUID.randomUUID() + getExt(file);
    }

    public static String getSubdir(String file) {
        return file.substring(0, 2);
    }

    public static String getExt(String originalFilename) {
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    @SneakyThrows
    public static void writeMeta(File metaFile, FileMeta meta) {
        String json = JSON.toJSONString(meta);
        Files.writeString(Paths.get(metaFile.getAbsolutePath()), json,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    @SneakyThrows
    public static void writeString(File metaFile, String json) {
        Files.writeString(Paths.get(metaFile.getAbsolutePath()), json,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    @SneakyThrows
    public static boolean download(File file, String fileUrl) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
//        String encodedUrl = URLEncoder.encode(fileUrl, "UTF-8");
//        encodedUrl = encodedUrl.replaceAll("\\+", "%20");
        HttpEntity<MultiValueMap<String, HttpEntity<?>>> httpEntity
                = new HttpEntity<>(headers);

        System.out.println(" == fileUrl = " + fileUrl);
        ResponseEntity<Resource> responseEntity =
                restTemplate.exchange(fileUrl, HttpMethod.GET, httpEntity, Resource.class);
        InputStream fis = new BufferedInputStream(responseEntity.getBody().getInputStream());
        byte[] buffer = new byte[16 * 1024];
        OutputStream outputStream = new FileOutputStream(file);
        while (fis.read(buffer) != -1) {
            outputStream.write(buffer);
        }
        outputStream.flush();
        outputStream.close();
        return true;
    }
}
