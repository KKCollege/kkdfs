package cn.kimmking.kkfs.syncer;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;

/**
 * sync file to backup server.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/7/10 下午8:45
 */

@Component
public class HttpSyncer {

    public final static String XFILENAME = "X-Filename";
    public final static String XORIGFILENAME = "X-Orig-Filename";

    public String sync(File file, String url, String originalFilename) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add(XFILENAME, file.getName());
        headers.add(XORIGFILENAME, originalFilename);

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new FileSystemResource(file));

        HttpEntity<MultiValueMap<String, HttpEntity<?>>> httpEntity
                = new HttpEntity<>(builder.build(), headers);

        ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
        String result = stringResponseEntity.getBody();
        System.out.println(" sync result = " + result);
        return result;
    }
}
