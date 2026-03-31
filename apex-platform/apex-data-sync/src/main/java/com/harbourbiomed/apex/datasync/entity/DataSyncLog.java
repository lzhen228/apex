package com.harbourbiomed.apex.datasync.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据同步日志实体
 * 
 * 记录每次数据同步的执行情况
 * 
 * @author Harbour BioMed
 */
@Data
@TableName("data_sync_log")
@Schema(description = "数据同步日志实体")
public class DataSyncLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键 ID")
    private Long id;

    /**
     * 模块名称（如：ci_tracking）
     */
    @TableField("module")
    @Schema(description = "模块名称")
    private String module;

    /**
     * 同步批次 ID
     */
    @TableField("sync_batch_id")
    @Schema(description = "同步批次 ID")
    private String syncBatchId;

    /**
     * 开始时间
     */
    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    /**
     * 同步状态（running | success | failed）
     */
    @TableField("status")
    @Schema(description = "同步状态")
    private String status;

    /**
     * 同步记录数量
     */
    @TableField("record_count")
    @Schema(description = "同步记录数量")
    private Long recordCount;

    /**
     * 错误信息
     */
    @TableField("error_message")
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 创建时间
     */
    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
