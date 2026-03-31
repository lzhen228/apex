package com.harbourbiomed.apex.datasync.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.harbourbiomed.apex.datasync.config.OssProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 阿里云 OSS 服务类
 * 
 * 提供文件下载、列表查询、删除等功能
 * 
 * @author Harbour BioMed
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssService {

    private final OssProperties ossProperties;

    /**
     * 从 OSS 下载文件到本地
     * 
     * @param ossPath OSS 文件路径
     * @param localPath 本地文件路径
     * @return 本地文件路径
     * @throws IOException 下载失败时抛出
     */
    public String downloadFile(String ossPath, String localPath) throws IOException {
        log.info("开始从 OSS 下载文件: {}", ossPath);
        
        OSS ossClient = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        
        try {
            // 创建 OSS 客户端
            ossClient = createOssClient();
            
            // 创建本地目录
            Path filePath = Paths.get(localPath);
            Files.createDirectories(filePath.getParent());
            
            // 获取 OSS 文件对象
            OSSObject ossObject = ossClient.getObject(ossProperties.getBucketName(), ossPath);
            inputStream = ossObject.getObjectContent();
            
            // 写入本地文件
            outputStream = new FileOutputStream(localPath);
            byte[] buffer = new byte[1024 * 1024]; // 1MB 缓冲区
            int bytesRead;
            long totalBytes = 0;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            
            log.info("文件下载成功: {} ({} bytes)", localPath, totalBytes);
            return localPath;
            
        } catch (Exception e) {
            log.error("从 OSS 下载文件失败: {}", ossPath, e);
            throw new IOException("从 OSS 下载文件失败: " + e.getMessage(), e);
        } finally {
            // 关闭流
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.warn("关闭输出流失败", e);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.warn("关闭输入流失败", e);
                }
            }
            // 关闭 OSS 客户端
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 列出指定前缀的文件
     * 
     * @param prefix 文件前缀
     * @return 文件列表
     */
    public List<String> listFiles(String prefix) {
        log.info("列出 OSS 文件，前缀: {}", prefix);
        
        OSS ossClient = null;
        List<String> files = new ArrayList<>();
        
        try {
            ossClient = createOssClient();
            
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest(ossProperties.getBucketName());
            listObjectsRequest.setPrefix(prefix);
            listObjectsRequest.setMaxKeys(1000);
            
            ObjectListing objectListing;
            do {
                objectListing = ossClient.listObjects(listObjectsRequest);
                
                for (OSSObjectSummary summary : objectListing.getObjectSummaries()) {
                    files.add(summary.getKey());
                }
                
                listObjectsRequest.setMarker(objectListing.getNextMarker());
            } while (objectListing.isTruncated());
            
            log.info("找到 {} 个文件", files.size());
            return files;
            
        } catch (Exception e) {
            log.error("列出 OSS 文件失败", e);
            return new ArrayList<>();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 删除 OSS 文件
     * 
     * @param ossPath OSS 文件路径
     * @return 是否删除成功
     */
    public boolean deleteFile(String ossPath) {
        log.info("删除 OSS 文件: {}", ossPath);
        
        OSS ossClient = null;
        
        try {
            ossClient = createOssClient();
            ossClient.deleteObject(ossProperties.getBucketName(), ossPath);
            log.info("文件删除成功: {}", ossPath);
            return true;
            
        } catch (Exception e) {
            log.error("删除 OSS 文件失败: {}", ossPath, e);
            return false;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 创建 OSS 客户端
     * 
     * @return OSS 客户端实例
     */
    private OSS createOssClient() {
        return new OSSClientBuilder().build(
            ossProperties.getEndpoint(),
            ossProperties.getAccessKeyId(),
            ossProperties.getAccessKeySecret()
        );
    }
}
