package cn.kimmking.kkfs;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * MQ syncer.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/7/16 下午5:57
 */

@Component
public class MQSyncer {

    @Value("${kkfs.downloadUrl}")
    private String url;

    @Value("${kkfs.path}")
    private String uploadPath;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    private final static String topic = "kkfs01";

    public void sync(String downloadUrl, FileMeta meta) {
        Message<String> message = MessageBuilder
                .withPayload(JSON.toJSONString(meta))
//                .setHeader("downloadUrl", downloadUrl)
                .build();
        rocketMQTemplate.send(topic, message);
        System.out.println(" ===>> send message: " + message);
    }

    @Service
    @RocketMQMessageListener(topic = "kkfs01", consumerGroup = "${kkfs.group}")
    public class FileMQSyncer implements RocketMQListener<MessageExt> {
        @Override
        public void onMessage(MessageExt messageExt) {
            System.out.println("====>> onMessage: ID = " + messageExt.getMsgId());
            String json = new String(messageExt.getBody());
            System.out.println("====>> onMessage: body = " + json);
            FileMeta meta = JSON.parseObject(json, FileMeta.class);

            String downloadUrl = meta.getDownloadUrl();
            if(downloadUrl == null || downloadUrl.isEmpty()) {
                System.out.println(" ===> downloadUrl is empty, ignore async download.");
                return;
            }
            if(downloadUrl.equals(url)) {
                System.out.println(" ===> the same server, ignore async download.");
                return;
            }

            System.out.println(" ===> the other server, async download start...");

            String dir = uploadPath + "/" + meta.getName().substring(0, 2);
            File metaFile = new File(dir, meta.getName() + ".meta");
            if(metaFile.exists()){
                System.out.println(" ===>>> meta file exist and ignore: " + metaFile.getAbsolutePath());
            } else {
                System.out.println(" ===>>> write meta file: " + metaFile.getAbsolutePath());
                FileUtils.writeString(metaFile, json); // 写meta文件
            }

            System.out.println(" ===> async mq: " + meta.getName());
            download(dir, meta.getName(), meta.getDownloadUrl(), meta.getSize());
        }
    }

    private void download(String dir, String filename, String downloadUrl, long size) {
        System.out.println(" ===>>> download " + filename + " from " + downloadUrl);
        File file = new File(dir, filename);
        if(file.exists() && file.length() == size) {
            System.out.println(" file exists and ignore: " + file.getAbsolutePath());
            return;
        }
        if(FileUtils.download(file, downloadUrl + "?name=" + filename)) {
            System.out.println(" ===>>> download success.");
        } else {
            System.out.println(" ===>>> download failed.");
        }
    }
}
