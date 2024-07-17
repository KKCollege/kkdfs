package cn.kimmking.kkfs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

import static cn.kimmking.kkfs.FileUtils.getMimeType;
import static cn.kimmking.kkfs.FileUtils.getUUIDFile;
import static cn.kimmking.kkfs.HttpSyncer.XFILENAME;
import static cn.kimmking.kkfs.HttpSyncer.XORIGFILENAME;

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

    @Value("${kkfs.autoMd5}")
    private boolean autoMd5;

    @SneakyThrows
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         HttpServletRequest request) {

        // 1. 处理文件
        boolean neeSync = false;
        String filename = request.getHeader(XFILENAME);
        // 同步文件到backup
        String originalFilename = file.getOriginalFilename();
        if(filename == null || filename.isEmpty())  { // upload上传文件
            neeSync = true;
            filename = getUUIDFile(originalFilename);
        } else { // 同步文件
            String xor = request.getHeader(XORIGFILENAME);
            if(xor !=null && !xor.isEmpty()) {
                originalFilename = xor;
            }
        }
        String subdir = FileUtils.getSubdir(filename);
        File dest = new File(uploadPath + "/" + subdir + "/" + filename);
        file.transferTo(dest);

        // 2. 处理meta
        FileMeta meta = new FileMeta();
        meta.setName(filename);
        meta.setOriginalFilename(originalFilename);
        meta.setSize(file.getSize());
        if(autoMd5) {
            meta.getTags().put("md5", DigestUtils.md5DigestAsHex(new FileInputStream(dest)));
        }

        // 2.1 存放到本地文件
        String metaName = filename + ".meta";
        File metaFile = new File(uploadPath + "/" + subdir + "/" + metaName);
        FileUtils.writeMeta(metaFile, meta);

        // 2.2 存到数据库
        // 2.3 存放到配置中心或注册中心，比如zk

        // 3. 同步到backup
        // 同步文件到backup
        if(neeSync)  {
            httpSyncer.sync(dest, backupUrl, originalFilename);
        }

        return filename;
    }



    @RequestMapping("/download")
    public void download(String name, HttpServletResponse response) {
        String subdir = FileUtils.getSubdir(name);
        String path = uploadPath + "/" + subdir + "/" + name;
        File file = new File(path);
        try {
            FileInputStream inputStream = new FileInputStream(file);
            InputStream fis = new BufferedInputStream(inputStream);
            byte[] buffer = new byte[16*1024];

            // 加一些response的头
            response.setCharacterEncoding("UTF-8");
            // response.setContentType("application/octet-stream");
            response.setContentType(getMimeType(name));
            // response.setHeader("Content-Disposition", "attachment;filename=" + name);
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

    @RequestMapping("/meta")
    public String meta(String name) {
        String subdir = FileUtils.getSubdir(name);
        String path = uploadPath + "/" + subdir + "/" + name + ".meta";
        File file = new File(path);
        try {
            return FileCopyUtils.copyToString(new FileReader(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
