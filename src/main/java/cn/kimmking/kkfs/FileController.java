package cn.kimmking.kkfs;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

import static cn.kimmking.kkfs.HttpSyncer.XFILENAME;

/**
 * file download and upload controller.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/7/10 下午8:10
 */

@RestController
public class FileController {

    @Value("${kkfs.path}")
    private String uploadPath;

    @Value("${kkfs.backupUrl}")
    private String backupUrl;

    @Autowired
    HttpSyncer httpSyncer;

    @SneakyThrows
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         HttpServletRequest request) {
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        boolean neeSync = false;
        String filename = request.getHeader(XFILENAME);
        // 同步文件到backup
        if(filename == null || filename.isEmpty())  {
            neeSync = true;
            filename = file.getOriginalFilename();
        }
        File dest = new File(uploadPath + "/" + filename);
        file.transferTo(dest);

        // 同步文件到backup
        if(neeSync)  {
            httpSyncer.sync(dest, backupUrl);
        }

        return filename;
    }

    @RequestMapping("/download")
    public void download(String name, HttpServletResponse response) {
        String path = uploadPath + "/" + name;
        File file = new File(path);
        try {
            FileInputStream inputStream = new FileInputStream(file);
            InputStream fis = new BufferedInputStream(inputStream);
            byte[] buffer = new byte[16*1024];

            // 加一些response的头
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + name);
            response.setHeader("Content-Length", String.valueOf(file.length()));

            // 读取文件信息，并逐段输出
            OutputStream outputStream = response.getOutputStream();
            while (fis.read(buffer) != -1) {
                outputStream.write(buffer);
            }
            outputStream.flush();
            fis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
