package cn.kimmking.kkfs;

import cn.kimmking.kkfs.config.KkfsConfigProperties;
import cn.kimmking.kkfs.meta.FileMeta;
import cn.kimmking.kkfs.syncer.HttpSyncer;
import cn.kimmking.kkfs.syncer.MQSyncer;
import cn.kimmking.kkfs.utils.FileUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * file download and upload controller.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/7/10 下午8:10
 */

@RestController
public class FileController {

    @Autowired
    KkfsConfigProperties configProperties;

    @Autowired
    HttpSyncer httpSyncer;

    @Autowired
    MQSyncer mqSyncer;

    @SneakyThrows
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        // 1. 处理文件
        boolean needSync = false;
        String filename = request.getHeader(HttpSyncer.XFILENAME);
        String originalFilename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) { // 如果这个为空则是正常上传
            needSync = true;
            filename = FileUtils.getUUIDFile(originalFilename);
        } else { // 如果走到这里，说明是主从同步文件
            String xor = request.getHeader(HttpSyncer.XORIGFILENAME);
            if (xor != null && !xor.isEmpty()) {
                originalFilename = xor;
            }
        }
        File dest = getFile(FileUtils.getSubdir(filename), filename);
        file.transferTo(dest); // 复制文件到制定位置
        // 2. 处理meta
        FileMeta meta = new FileMeta(filename, originalFilename, file.getSize(), configProperties.getDownloadUrl());
        if (configProperties.isAutoMd5()) {
            meta.getTags().put("md5", DigestUtils.md5DigestAsHex(new FileInputStream(dest)));
        }
        // 2.1 存放到本地文件
        FileUtils.writeMeta(new File(dest.getAbsolutePath() + ".meta"), meta);
        // 3. 同步到backup
        if (needSync) {
            if (configProperties.isSyncBackup()) {
                try {
                    httpSyncer.sync(dest, configProperties.getBackupUrl(), originalFilename);
                } catch (Exception exception) {
                    exception.printStackTrace();
                    mqSyncer.sync(meta); // 同步失败则转异步处理
                }
            } else {
                mqSyncer.sync(meta);
            }
        }
        return filename;
    }

    private File getFile(String subdir, String filename) {
        return new File(configProperties.getUploadPath() + "/" + subdir + "/" + filename);
    }

    @SneakyThrows
    @RequestMapping("/download")
    public void download(String name, HttpServletResponse response) {
        File file = getFile(FileUtils.getSubdir(name),name);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(FileUtils.getMimeType(name));
        // response.setHeader("Content-Disposition", "attachment;filename=" + name);
        response.setHeader("Content-Length", String.valueOf(file.length()));
        FileUtils.output(file, response.getOutputStream());
    }

    @SneakyThrows
    @RequestMapping("/meta")
    public String meta(String name) {
        return FileUtils.readString(getFile(FileUtils.getSubdir(name),name));
    }
}