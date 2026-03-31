-- Apex 平台数据库初始化脚本
-- 此脚本在 PostgreSQL 容器首次启动时自动执行
-- 文件名：01-init-schema.sql

-- 创建扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- 创建表空间（可选）
-- CREATE TABLESPACE apex_data LOCATION '/var/lib/postgresql/data/apex';

-- 设置时区
SET timezone = 'Asia/Shanghai';

-- 创建自定义枚举类型
CREATE TYPE user_role AS ENUM ('ADMIN', 'USER', 'GUEST');
CREATE TYPE data_source AS ENUM ('CHIOT', 'CLINICALTRIALS', 'OTHER');
CREATE TYPE target_phase AS ENUM ('Discovery', 'Preclinical', 'Phase I', 'Phase II', 'Phase III', 'Approved');
CREATE TYPE disease_level AS ENUM ('L1', 'L2', 'L3');

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100),
    role user_role NOT NULL DEFAULT 'USER',
    department VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);

-- 创建疾病表
CREATE TABLE IF NOT EXISTS diseases (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    ci_id VARCHAR(50),
    ci_parent_id VARCHAR(50),
    level disease_level,
    category VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建靶点表
CREATE TABLE IF NOT EXISTS targets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    symbol VARCHAR(100),
    uniprot_id VARCHAR(50),
    description TEXT,
    ci_id VARCHAR(50),
    isci_expert_tagged BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建药物表
CREATE TABLE IF NOT EXISTS drugs (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    ci_id VARCHAR(50),
    approval_year INTEGER,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建靶点-疾病-药物关联表
CREATE TABLE IF NOT EXISTS target_disease_drugs (
    id BIGSERIAL PRIMARY KEY,
    target_id BIGINT NOT NULL REFERENCES targets(id) ON DELETE CASCADE,
    disease_id BIGINT NOT NULL REFERENCES diseases(id) ON DELETE CASCADE,
    drug_id BIGINT NOT NULL REFERENCES drugs(id) ON DELETE CASCADE,
    phase target_phase NOT NULL,
    data_source data_source NOT NULL,
    ci_tracking_info JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(target_id, disease_id, drug_id, phase, data_source)
);

-- 创建过滤预设表
CREATE TABLE IF NOT EXISTS filter_presets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    module VARCHAR(50) NOT NULL, -- 'competition', 'progress' 等
    filters JSONB NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建审计日志表
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_diseases_ci_id ON diseases(ci_id);
CREATE INDEX IF NOT EXISTS idx_diseases_ci_parent_id ON diseases(ci_parent_id);
CREATE INDEX IF NOT EXISTS idx_targets_ci_id ON targets(ci_id);
CREATE INDEX IF NOT EXISTS idx_drugs_ci_id ON drugs(ci_id);
CREATE INDEX IF NOT EXISTS idx_tdd_target_disease ON target_disease_drugs(target_id, disease_id);
CREATE INDEX IF NOT EXISTS idx_tdd_disease ON target_disease_drugs(disease_id);
CREATE INDEX IF NOT EXISTS idx_tdd_drug ON target_disease_drugs(drug_id);
CREATE INDEX IF NOT EXISTS idx_tdd_phase ON target_disease_drugs(phase);
CREATE INDEX IF NOT EXISTS idx_tdd_data_source ON target_disease_drugs(data_source);
CREATE INDEX IF NOT EXISTS idx_filter_presets_user ON filter_presets(user_id);
CREATE INDEX IF NOT EXISTS idx_filter_presets_module ON filter_presets(module);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at);

-- 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为需要的表创建更新时间触发器
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_diseases_updated_at BEFORE UPDATE ON diseases
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_targets_updated_at BEFORE UPDATE ON targets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_drugs_updated_at BEFORE UPDATE ON drugs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_target_disease_drugs_updated_at BEFORE UPDATE ON target_disease_drugs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_filter_presets_updated_at BEFORE UPDATE ON filter_presets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 插入初始数据

-- 创建默认管理员用户（密码：admin123，请立即修改）
INSERT INTO users (username, password, email, full_name, role, department)
VALUES (
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'admin@harbourbio.com',
    '系统管理员',
    'ADMIN',
    'IT部'
) ON CONFLICT (username) DO NOTHING;

-- 插入示例数据（开发环境）

-- 示例：疾病数据
INSERT INTO diseases (name, ci_id, ci_parent_id, level, category) VALUES
    ('肿瘤性疾病', 'c001', NULL, 'L1', 'Oncology'),
    ('肺癌', 'c001001', 'c001', 'L2', 'Oncology'),
    ('非小细胞肺癌', 'c001001001', 'c001001', 'L3', 'Oncology'),
    ('小细胞肺癌', 'c001001002', 'c001001', 'L3', 'Oncology'),
    ('血液系统疾病', 'c002', NULL, 'L1', 'Hematology'),
    ('淋巴瘤', 'c002001', 'c002', 'L2', 'Hematology'),
    ('白血病', 'c002002', 'c002', 'L2', 'Hematology')
ON CONFLICT DO NOTHING;

-- 示例：靶点数据
INSERT INTO targets (name, symbol, uniprot_id, description, ci_id) VALUES
    ('Tumor Necrosis Factor', 'TNF', 'P01375', 'A cytokine involved in systemic inflammation', 't001'),
    ('Epidermal Growth Factor Receptor', 'EGFR', 'P00533', 'A receptor tyrosine kinase', 't002'),
    ('Programmed Death-1', 'PD-1', 'Q15116', 'An immune checkpoint receptor', 't003'),
    ('Programmed Death Ligand-1', 'PD-L1', 'Q9NZQ7', 'An immune checkpoint ligand', 't004')
ON CONFLICT DO NOTHING;

-- 示例：药物数据
INSERT INTO drugs (name, ci_id, approval_year) VALUES
    ('Infliximab', 'd001', 1998),
    ('Adalimumab', 'd002', 2002),
    ('Etanercept', 'd003', 1998),
    ('Gefitinib', 'd004', 2002),
    ('Pembrolizumab', 'd005', 2014)
ON CONFLICT DO NOTHING;

-- 授予必要的权限
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO apex_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO apex_user;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO apex_user;

-- 显示初始化完成信息
DO $$
BEGIN
    RAISE NOTICE 'Apex platform database schema initialized successfully.';
END $$;
