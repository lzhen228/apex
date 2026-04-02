package com.harbourbiomed.apex.datasync.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "apex.data-sync")
public class DataSyncProperties {

    /**
     * 服务器上 Parquet 文件的绝对路径，每天凌晨由医药魔方推送至此目录
     */
    private String parquetFilePath = "/home/pharmcube2harbour_ci_tracking_info_0.parquet";

    /**
     * 批量写入 PostgreSQL 的每批次行数
     */
    private int batchSize = 500;
}
