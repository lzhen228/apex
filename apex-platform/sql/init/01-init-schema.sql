-- Apex 平台数据库初始化脚本 - Schema 定义
-- 版本: v1.0.0
-- 说明: 创建所有数据库表、索引和约束
-- 数据库: PostgreSQL 16.x

-- 创建数据库（如果不存在）
-- 注意：在 PostgreSQL 中，CREATE DATABASE 不能在事务中执行
-- 请在执行此脚本前手动创建数据库，或以超级用户身份执行：
-- CREATE DATABASE IF NOT EXISTS apex;

-- 设置搜索路径
SET search_path TO public;

-- ============================================================================
-- 1. 研发阶段分值映射表
-- 用途: 定义研发阶段与分值的对应关系，用于矩阵颜色编码和排序
-- ============================================================================
CREATE TABLE IF NOT EXISTS phase_score_mapping (
    phase_name   VARCHAR(50) PRIMARY KEY,
    score        NUMERIC(3,1) NOT NULL,
    sort_order   SMALLINT     NOT NULL,
    color_code   VARCHAR(7)   -- 前端颜色编码，如 #10B981
);

COMMENT ON TABLE phase_score_mapping IS '研发阶段分值映射表，定义各研发阶段的分值、排序和颜色编码';
COMMENT ON COLUMN phase_score_mapping.phase_name IS '研发阶段名称，主键';
COMMENT ON COLUMN phase_score_mapping.score IS '研发阶段分值，用于矩阵计算和排序';
COMMENT ON COLUMN phase_score_mapping.sort_order IS '显示排序顺序';
COMMENT ON COLUMN phase_score_mapping.color_code IS '前端颜色编码，用于矩阵格子的颜色显示';

-- ============================================================================
-- 2. 治疗领域表
-- 用途: 维度表，存储治疗领域信息，支持治疗领域→疾病的级联选择
-- ============================================================================
CREATE TABLE IF NOT EXISTS therapeutic_area (
    id          SERIAL       PRIMARY KEY,
    name_en     VARCHAR(100) NOT NULL,
    name_cn     VARCHAR(100),
    sort_order  SMALLINT     DEFAULT 0
);

COMMENT ON TABLE therapeutic_area IS '治疗领域维度表';
COMMENT ON COLUMN therapeutic_area.id IS '治疗领域ID，主键';
COMMENT ON COLUMN therapeutic_area.name_en IS '治疗领域英文名称';
COMMENT ON COLUMN therapeutic_area.name_cn IS '治疗领域中文名称';
COMMENT ON COLUMN therapeutic_area.sort_order IS '显示排序顺序';

-- ============================================================================
-- 3. 疾病表
-- 用途: 维度表，存储疾病信息，与治疗领域关联
-- ============================================================================
CREATE TABLE IF NOT EXISTS disease (
    id          SERIAL       PRIMARY KEY,
    ta_id       INTEGER      REFERENCES therapeutic_area(id),
    name_en     VARCHAR(300) NOT NULL,
    name_cn     VARCHAR(300),
    abbreviation VARCHAR(100)
);

COMMENT ON TABLE disease IS '疾病维度表';
COMMENT ON COLUMN disease.id IS '疾病ID，主键';
COMMENT ON COLUMN disease.ta_id IS '所属治疗领域ID，外键关联 therapeutic_area';
COMMENT ON COLUMN disease.name_en IS '疾病英文名称';
COMMENT ON COLUMN disease.name_cn IS '疾病中文名称';
COMMENT ON COLUMN disease.abbreviation IS '疾病缩写/简称';

-- ============================================================================
-- 4. 用户表
-- 用途: 存储系统用户信息，支持认证和授权
-- ============================================================================
CREATE TABLE IF NOT EXISTS sys_user (
    id             SERIAL       PRIMARY KEY,
    username       VARCHAR(50)  UNIQUE NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    display_name   VARCHAR(100),
    role           VARCHAR(20)  DEFAULT 'analyst',  -- 'admin' or 'analyst'
    status         SMALLINT     DEFAULT 1,          -- 1=active, 0=inactive
    created_at     TIMESTAMP    DEFAULT NOW(),
    updated_at     TIMESTAMP    DEFAULT NOW()
);

COMMENT ON TABLE sys_user IS '系统用户表';
COMMENT ON COLUMN sys_user.id IS '用户ID，主键';
COMMENT ON COLUMN sys_user.username IS '用户名，唯一';
COMMENT ON COLUMN sys_user.password_hash IS '密码哈希值（BCrypt）';
COMMENT ON COLUMN sys_user.display_name IS '用户显示名称';
COMMENT ON COLUMN sys_user.role IS '用户角色：admin=管理员，analyst=分析师';
COMMENT ON COLUMN sys_user.status IS '用户状态：1=活跃，0=停用';
COMMENT ON COLUMN sys_user.created_at IS '创建时间';
COMMENT ON COLUMN sys_user.updated_at IS '更新时间';

-- ============================================================================
-- 5. 竞争情报主表（查询宽表）
-- 用途: 存储从医药魔方同步的竞争情报数据，是矩阵查询的核心表
-- ============================================================================
CREATE TABLE IF NOT EXISTS ci_tracking_latest (
    id                              BIGSERIAL PRIMARY KEY,
    drug_id                         VARCHAR(50),
    drug_name_en                    VARCHAR(200),
    drug_name_cn                    VARCHAR(200),
    targets                         TEXT[]       NOT NULL DEFAULT '{}',
    targets_raw                     VARCHAR(500),
    disease_id                      INTEGER      REFERENCES disease(id),
    harbour_indication_name         VARCHAR(300),
    ta                              VARCHAR(100),
    moa                             VARCHAR(500),
    originator                      VARCHAR(300),
    research_institute              VARCHAR(1000),
    global_highest_phase            VARCHAR(50),
    global_highest_phase_score      NUMERIC(3,1),
    indication_top_global_latest_stage VARCHAR(50),
    indication_top_global_start_date   DATE,
    highest_trial_id                VARCHAR(100),
    highest_trial_phase             VARCHAR(50),
    nct_id                          VARCHAR(50),
    data_source                     VARCHAR(100),
    sync_batch_id                   VARCHAR(50)  NOT NULL,
    synced_at                       TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_at                      TIMESTAMP    NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE ci_tracking_latest IS '竞争情报主表，存储从医药魔方同步的数据';
COMMENT ON COLUMN ci_tracking_latest.id IS '记录ID，主键';
COMMENT ON COLUMN ci_tracking_latest.drug_id IS '药品ID';
COMMENT ON COLUMN ci_tracking_latest.drug_name_en IS '药品英文名称';
COMMENT ON COLUMN ci_tracking_latest.drug_name_cn IS '药品中文名称';
COMMENT ON COLUMN ci_tracking_latest.targets IS '靶点数组，用于 GIN 索引查询';
COMMENT ON COLUMN ci_tracking_latest.targets_raw IS '靶点原始字符串（逗号分隔）';
COMMENT ON COLUMN ci_tracking_latest.disease_id IS '疾病ID，外键关联 disease';
COMMENT ON COLUMN ci_tracking_latest.harbour_indication_name IS '疾病名称';
COMMENT ON COLUMN ci_tracking_latest.ta IS '治疗领域';
COMMENT ON COLUMN ci_tracking_latest.moa IS '作用机制';
COMMENT ON COLUMN ci_tracking_latest.originator IS '原研机构';
COMMENT ON COLUMN ci_tracking_latest.research_institute IS '所有研究机构（逗号分隔）';
COMMENT ON COLUMN ci_tracking_latest.global_highest_phase IS '全球最高研发阶段';
COMMENT ON COLUMN ci_tracking_latest.global_highest_phase_score IS '全球最高研发阶段分值';
COMMENT ON COLUMN ci_tracking_latest.indication_top_global_latest_stage IS '疾病全球最高阶段';
COMMENT ON COLUMN ci_tracking_latest.indication_top_global_start_date IS '最高阶段开始时间';
COMMENT ON COLUMN ci_tracking_latest.highest_trial_id IS '最高临床试验ID';
COMMENT ON COLUMN ci_tracking_latest.highest_trial_phase IS '最高临床试验阶段';
COMMENT ON COLUMN ci_tracking_latest.nct_id IS 'NCT编号（临床试验注册号）';
COMMENT ON COLUMN ci_tracking_latest.data_source IS '数据来源';
COMMENT ON COLUMN ci_tracking_latest.sync_batch_id IS '同步批次ID';
COMMENT ON COLUMN ci_tracking_latest.synced_at IS '同步时间';
COMMENT ON COLUMN ci_tracking_latest.created_at IS '记录创建时间';

-- 创建索引：加速靶点数组查询（GIN 索引）
CREATE INDEX IF NOT EXISTS idx_ci_tracking_targets 
    ON ci_tracking_latest USING GIN (targets);

COMMENT ON INDEX idx_ci_tracking_targets IS 'GIN 索引，加速靶点数组查询（targets @> operator）';

-- 创建索引：加速矩阵查询（组合索引）
CREATE INDEX IF NOT EXISTS idx_ci_tracking_matrix 
    ON ci_tracking_latest (ta, harbour_indication_name, global_highest_phase_score DESC);

COMMENT ON INDEX idx_ci_tracking_matrix IS '组合索引，加速矩阵查询（按治疗领域、疾病、阶段分值排序）';

-- 创建索引：疾病 + 靶点组合索引
CREATE INDEX IF NOT EXISTS idx_ci_tracking_disease_target 
    ON ci_tracking_latest (disease_id, targets);

COMMENT ON INDEX idx_ci_tracking_disease_target IS '疾病和靶点组合索引，支持按疾病和靶点过滤查询';

-- ============================================================================
-- 6. 筛选条件预设表
-- 用途: 存储用户保存的筛选条件预设，支持快速加载常用查询条件
-- ============================================================================
CREATE TABLE IF NOT EXISTS filter_preset (
    id          SERIAL       PRIMARY KEY,
    user_id     INTEGER      NOT NULL REFERENCES sys_user(id),
    name        VARCHAR(100) NOT NULL,
    module      VARCHAR(50)  NOT NULL,  -- 'competition' | 'progress'
    conditions  JSONB        NOT NULL,
    'is_default'  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE filter_preset IS '筛选条件预设表，用户可保存常用查询条件';
COMMENT ON COLUMN filter_preset.id IS '预设ID，主键';
COMMENT ON COLUMN filter_preset.user_id IS '用户ID，外键关联 sys_user';
COMMENT ON COLUMN filter_preset.name IS '预设名称';
COMMENT ON COLUMN filter_preset.module IS '模块标识：competition=靶点组合竞争格局，progress=靶点研发进展格局';
COMMENT ON COLUMN filter_preset.conditions IS '筛选条件JSON（疾病ID列表、阶段列表等）';
COMMENT ON COLUMN filter_preset.is_default IS '是否默认预设';
COMMENT ON COLUMN filter_preset.created_at IS '创建时间';
COMMENT ON COLUMN filter_preset.updated_at IS '更新时间';

-- ============================================================================
-- 7. 药品管线信息表（预留）
-- 用途: 预留表，后续迭代用于存储药品管线详细信息
-- ============================================================================
CREATE TABLE IF NOT EXISTS drug_pipeline_info (
    id                      BIGSERIAL PRIMARY KEY,
    drug_name_en            VARCHAR(200),
    drug_name_cn            VARCHAR(200),
    targets                 TEXT[],
    company_names_originator VARCHAR(500),
    pharmacological_name    VARCHAR(200),
    sync_batch_id           VARCHAR(50)  NOT NULL,
    synced_at               TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_at              TIMESTAMP    NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE drug_pipeline_info IS '药品管线信息表（预留），后续迭代使用';
COMMENT ON COLUMN drug_pipeline_info.id IS '记录ID，主键';
COMMENT ON COLUMN drug_pipeline_info.drug_name_en IS '药品英文名称';
COMMENT ON COLUMN drug_pipeline_info.drug_name_cn IS '药品中文名称';
COMMENT ON COLUMN drug_pipeline_info.targets IS '靶点数组';
COMMENT ON COLUMN drug_pipeline_info.company_names_originator IS '原研公司名称';
COMMENT ON COLUMN drug_pipeline_info.pharmacological_name IS '药理学名称';
COMMENT ON COLUMN drug_pipeline_info.sync_batch_id IS '同步批次ID';
COMMENT ON COLUMN drug_pipeline_info.synced_at IS '同步时间';
COMMENT ON COLUMN drug_pipeline_info.created_at IS '记录创建时间';

-- ============================================================================
-- 脚本执行完成
-- ============================================================================
-- Schema 初始化完成，接下来执行 02-insert-initial-data.sql 插入初始数据
