-- ═══════════════════════════════════════════════════════════════════
-- Apex 数据库初始化脚本
-- 执行环境：PostgreSQL 16.x
-- 说明：首次部署时执行，后续迭代通过 migration 脚本（V2__...）追加
-- ═══════════════════════════════════════════════════════════════════

-- ─── 治疗领域维度表 ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS therapeutic_area (
    id         SERIAL PRIMARY KEY,
    name_en    VARCHAR(100) NOT NULL,
    name_cn    VARCHAR(100),
    sort_order SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uq_therapeutic_area_name UNIQUE (name_en)
);

-- ─── 疾病维度表 ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS disease (
    id           SERIAL PRIMARY KEY,
    ta_id        INTEGER      NOT NULL REFERENCES therapeutic_area (id),
    name_en      VARCHAR(300) NOT NULL,
    name_cn      VARCHAR(300),
    abbreviation VARCHAR(100),
    CONSTRAINT uq_disease_ta_name UNIQUE (ta_id, name_en)
);

-- ─── 研发阶段分值映射表 ───────────────────────────────────────────
CREATE TABLE IF NOT EXISTS phase_score_mapping (
    phase_name VARCHAR(50)  PRIMARY KEY,
    score      NUMERIC(3,1) NOT NULL,
    sort_order SMALLINT     NOT NULL,
    color_code VARCHAR(7)
);

INSERT INTO phase_score_mapping VALUES
    ('Approved',     4.0, 1, '#10B981'),
    ('BLA',          3.5, 2, '#06B6D4'),
    ('Phase III',    3.0, 3, '#3B82F6'),
    ('Phase II/III', 2.5, 4, '#6366F1'),
    ('Phase II',     2.0, 5, '#8B5CF6'),
    ('Phase I/II',   1.5, 6, '#A855F7'),
    ('Phase I',      1.0, 7, '#D946EF'),
    ('IND',          0.5, 8, '#F59E0B'),
    ('PreClinical',  0.1, 9, '#6B7280')
ON CONFLICT (phase_name) DO NOTHING;

-- ─── 竞争情报主表 ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ci_tracking_latest (
    id                                 BIGSERIAL    PRIMARY KEY,
    drug_id                            VARCHAR(50),
    drug_name_en                       VARCHAR(200),
    drug_name_cn                       VARCHAR(200),
    targets                            TEXT[]       NOT NULL DEFAULT '{}',
    targets_raw                        VARCHAR(500),
    disease_id                         INTEGER      REFERENCES disease (id),
    harbour_indication_name            VARCHAR(300),
    ta                                 VARCHAR(100),
    moa                                VARCHAR(500),
    originator                         VARCHAR(300),
    research_institute                 VARCHAR(1000),
    global_highest_phase               VARCHAR(50),
    global_highest_phase_score         NUMERIC(3,1),
    indication_top_global_latest_stage VARCHAR(50),
    indication_top_global_start_date   DATE,
    highest_trial_id                   VARCHAR(100),
    highest_trial_phase                VARCHAR(50),
    nct_id                             VARCHAR(50),
    data_source                        VARCHAR(100),
    sync_batch_id                      VARCHAR(50)  NOT NULL,
    synced_at                          TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_at                         TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ─── 索引 ──────────────────────────────────────────────────────────
-- 靶点数组 GIN 索引，加速 targets @> '{TNF}' 精确匹配
CREATE INDEX IF NOT EXISTS idx_ci_tracking_targets
    ON ci_tracking_latest USING GIN (targets);

-- 矩阵查询组合索引
CREATE INDEX IF NOT EXISTS idx_ci_tracking_matrix
    ON ci_tracking_latest (ta, harbour_indication_name, global_highest_phase_score DESC);

-- 疾病 + 靶点组合索引
CREATE INDEX IF NOT EXISTS idx_ci_tracking_disease_target
    ON ci_tracking_latest (disease_id, targets);

-- ─── 用户表 ───────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS sys_user (
    id            SERIAL       PRIMARY KEY,
    username      VARCHAR(64)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    display_name  VARCHAR(100),
    role          VARCHAR(20)  NOT NULL DEFAULT 'analyst',
    status        SMALLINT     NOT NULL DEFAULT 1,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    deleted       SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uq_sys_user_username UNIQUE (username)
);

-- ─── 筛选条件预设表 ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS filter_preset (
    id         SERIAL       PRIMARY KEY,
    user_id    INTEGER      NOT NULL REFERENCES sys_user (id),
    name       VARCHAR(100) NOT NULL,
    module     VARCHAR(50)  NOT NULL,
    conditions JSONB        NOT NULL,
    is_default BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);
