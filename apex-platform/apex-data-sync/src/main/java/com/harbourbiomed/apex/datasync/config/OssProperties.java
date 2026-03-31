package com.harbourbiomed.apex.datasync.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿阿 OSS 配置属性
 * 
 * @author Harbour BioMed
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {

    /**
     * OSS 访问地址
     */
    private String endpoint;

    /**
     * 访问密钥 ID
     */
    private String accessKeyId;

    /**
     * 访问密钥
     */
    private String accessKeySecret;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * CI 追踪信息 Parquet 文件路径
     */
    private String ciTrackingParquetPath = "pharmcube2harbour_ci_tracking_info_0.parquet";

    /**
     * 本地临时文件存储目录
     */
    private String localTempDir = "/tmp/apex-sync";
}
