-- =============================================
-- 数据同步日志表 DDL
-- =============================================

-- 创建数据同步日志表
CREATE TABLE IF NOT EXISTS data_sync_log (
    id BIGSERIAL PRIMARY KEY,
    module VARCHAR(50) NOT NULL,
    sync_batch_id VARCHAR(50) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    status VARCHAR(20) NOT NULL CHECK (status IN ('running', 'success', 'failed')),
    record_count BIGINT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_data_sync_log_module ON data_sync_log(module);
CREATE INDEX IF NOT EXISTS idx_data_sync_log_batch_id ON data_sync_log(sync_batch_id);
CREATE INDEX IF NOT EXISTS idx_data_sync_log_created_at ON data_sync_log(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_data_sync_log_status ON data_sync_log(status);

-- 添加表注释
COMMENT ON TABLE data_sync_log IS '数据同步日志表，记录每次数据同步的执行情况';
COMMENT ON COLUMN data_sync_log.id IS '主键 ID';
COMMENT ON COLUMN data_sync_log.module IS '模块名称（如：ci_tracking）';
COMMENT ON COLUMN data_sync_log.sync_batch_id IS '同步批次 ID';
COMMENT ON COLUMN data_sync_log.start_time IS '同步开始时间';
COMMENT ON COLUMN data_sync_log.end_time IS '同步结束时间';
COMMENT ON COLUMN data_sync_log.status IS '同步状态（running | success | failed）';
COMMENT ON COLUMN data_sync_log.record_count IS '同步记录数量';
COMMENT ON COLUMN data_sync_log.error_message IS '错误信息';
COMMENT ON COLUMN data_sync_log.created_at IS '创建时间';

-- =============================================
-- CI 追踪信息表（如果不存在）
-- =============================================

-- 创建 CI 追踪信息表
CREATE TABLE IF NOT EXISTS ci_tracking_info (
    id BIGSERIAL PRIMARY KEY,
    drug_id VARCHAR(100),
    drug_name_en VARCHAR(500),
    drug_name_cn VARCHAR(500),
    targets TEXT[],
    targets_raw TEXT,
    disease_id VARCHAR(100),
    harbour_indication_name VARCHAR(500),
    ta VARCHAR(100),
    moa VARCHAR(500),
    originator VARCHAR(500),
    research_institute VARCHAR(500),
    global_highest_phase VARCHAR(100),
    global_highest_phase_score INTEGER,
    indication_top_global_latest_stage VARCHAR(100),
    indication_top_global_start_date VARCHAR(100),
    highest_trial_id VARCHAR(100),
    highest_trial_phase VARCHAR(100),
    nct_id VARCHAR(100),
    data_source VARCHAR(100),
    sync_batch_id VARCHAR(50),
    synced_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_ci_tracking_drug_id ON ci_tracking_info(drug_id);
CREATE INDEX IF NOT EXISTS idx_ci_tracking_disease_id ON ci_tracking_info(disease_id);
CREATE INDEX IF NOT EXISTS idx_ci_tracking_sync_batch_id ON ci_tracking_info(sync_batch_id);
CREATE INDEX IF NOT EXISTS idx_ci_tracking_synced_at ON ci_tracking_info(synced_at DESC);
CREATE INDEX IF NOT EXISTS idx_ci_tracking_targets ON ci_tracking_info USING GIN(targets);

-- 添加表注释
COMMENT ON TABLE ci_tracking_info IS 'CI 追踪信息表，存储从 Parquet 文件中读取的竞争情报数据';
COMMENT ON COLUMN ci_tracking_info.id IS '主键 ID';
COMMENT ON COLUMN ci_tracking_info.drug_id IS '药物 ID';
COMMENT ON COLUMN ci_tracking_info.drug_name_en IS '药物英文名称';
COMMENT ON COLUMN ci_tracking_info.drug_name_cn IS '药物中文名称';
COMMENT ON COLUMN ci_tracking_info.targets IS '靶点列表（PostgreSQL 数组类型）';
COMMENT ON COLUMN ci_tracking_info.targets_raw IS '靶点原始字符串';
COMMENT ON COLUMN ci_tracking_info.disease_id IS '疾病 ID';
COMMENT ON COLUMN ci_tracking_info.harbour_indication_name IS '港口适应症名称';
COMMENT ON COLUMN ci_tracking_info.ta IS '治疗领域（Therapeutic Area）';
COMMENT ON COLUMN ci_tracking_info.moa IS '作用机制（Mechanism of Action）';
COMMENT ON COLUMN ci_tracking_info.originator IS '原研机构';
COMMENT ON COLUMN ci_tracking_info.research_institute IS '研究机构';
COMMENT ON COLUMN ci_tracking_info.global_highest_phase IS '全球最高研发阶段';
COMMENT ON COLUMN ci_tracking_info.global_highest_phase_score IS '全球最高研发阶段分值';
COMMENT ON COLUMN ci_tracking_info.indication_top_global_latest_stage IS '首要适应症全球最新阶段';
COMMENT ON COLUMN ci_tracking_info.indication_top_global_start_date IS '首要适应症全球开始日期';
COMMENT ON COLUMN ci_tracking_info.highest_trial_id IS '最高试验 ID';
COMMENT ON COLUMN ci_tracking_info.highest_trial_phase IS '最高试验阶段';
COMMENT ON COLUMN ci_tracking_info.nct_id IS 'NCT 试验编号';
COMMENT ON COLUMN ci_tracking_info.data_source IS '数据来源';
COMMENT ON COLUMN ci_tracking_info.sync_batch_id IS '同步批次 ID';
COMMENT ON COLUMN ci_tracking_info.synced_at IS '同步时间';
COMMENT ON COLUMN ci_tracking_info.created_at IS '创建时间';
