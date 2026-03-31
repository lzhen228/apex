-- Apex 平台数据库初始化脚本 - 初始数据
-- 版本: v1.0.0
-- 说明: 插入初始数据（研发阶段分值映射等）
-- 依赖: 01-init-schema.sql 必须先执行
-- 数据库: PostgreSQL 16.x

-- 设置搜索路径
SET search_path TO public;

-- ============================================================================
-- 1. 插入研发阶段分值映射数据
-- 说明: 定义 9 个研发阶段的分值、排序和颜色编码
-- 用途: 用于矩阵颜色编码、排序和阶段筛选
-- 插入策略: 使用 TRUNCATE + INSERT 确保数据一致性
-- ============================================================================
TRUNCATE TABLE phase_score_mapping;

INSERT INTO phase_score_mapping (phase_name, score, sort_order, color_code) VALUES
    ('Approved',     4.0, 1, '#10B981'),
    ('BLA',          3.5, 2, '#06B6D4'),
    ('Phase III',    3.0, 3, '#3B82F6'),
    ('Phase II/III', 2.5, 4, '#6366F1'),
    ('Phase II',     2.0, 5, '#8B5CF6'),
    ('Phase I/II',   1.5, 6, '#A855F7'),
    ('Phase I',      1.0, 7, '#D946EF'),
    ('IND',          0.5, 8, '#F59E0B'),
    ('PreClinical',  0.1, 9, '#6B7280');

-- 验证插入的数据
DO $$
DECLARE
    v_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM phase_score_mapping;
    IF v_count != 9 THEN
        RAISE EXCEPTION '研发阶段分值映射数据插入失败，预期 9 条记录，实际 % 条', v_count;
    END IF;
    RAISE NOTICE '研发阶段分值映射数据插入成功，共 % 条记录', v_count;
END $$;

-- ============================================================================
-- 2. 插入示例治疗领域数据（可选）
-- 说明: 根据需求文档中的示例，插入常见治疗领域
-- 注意: 这些是示例数据，实际数据应通过 ETL 从医药魔方同步
-- ============================================================================
INSERT INTO therapeutic_area (name_en, name_cn, sort_order) VALUES
    ('GI',          '消化系统',  1),
    ('Dermatology', '皮肤科',    2),
    ('Oncology',    '肿瘤科',    3),
    ('Immunology',  '免疫学',    4),
    ('Respiratory', '呼吸系统',  5)
ON CONFLICT DO NOTHING;

-- ============================================================================
-- 3. 插入示例疾病数据（可选）
-- 说明: 根据需求文档中的示例，插入常见疾病
-- 注意: 这些是示例数据，实际数据应通过 ETL 从医药魔方同步
-- ============================================================================
INSERT INTO disease (ta_id, name_en, name_cn, abbreviation) VALUES
    (1, 'Celiac Disease',                                     '乳糜泻',                     NULL),
    (1, 'Gastroesophageal Reflux Disease (GERD)',           '胃食管反流病',               'GERD'),
    (1, 'Hepatic Fibrosis (HF)',                             '肝纤维化',                   'HF'),
    (1, 'Primary Biliary Cholangitis (PBC)',                 '原发性胆汁性胆管炎',         'PBC'),
    (2, 'Hidradenitis Suppurativa (HS)',                     '化脓性汗腺炎',               'HS'),
    (2, 'Pemphigus Vulgaris',                                '寻常型天疱疮',               NULL),
    (2, 'Psoriasis (PsO)',                                   '银屑病',                     'PsO'),
    (2, 'Rosacea',                                           '玫瑰痤疮',                   NULL)
ON CONFLICT DO NOTHING;

-- ============================================================================
-- 4. 插入默认管理员用户
-- 说明: 创建默认管理员账号
-- 用户名: admin
-- 密码: admin123
-- 角色: admin
-- 状态: active (1)
-- ============================================================================
INSERT INTO sys_user (username, password_hash, display_name, role, status) VALUES
    ('admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TqMQZgSSw5eMXH8zGqJZJWvCQr6', '系统管理员', 'admin', 1)
ON CONFLICT (username) DO NOTHING;

-- ============================================================================
-- 数据插入完成
-- ============================================================================
DO $$
BEGIN
    RAISE NOTICE '初始数据插入完成';
    RAISE NOTICE '   - 研发阶段分值映射: 9 条记录';
    RAISE NOTICE '   - 示例治疗领域: 5 条记录（示例数据）';
    RAISE NOTICE '   - 示例疾病: 8 条记录（示例数据）';
    RAISE NOTICE '   - 默认管理员: 1 条记录（密码需在应用中设置）';
    RAISE NOTICE '提示: 治疗领域和疾病的示例数据仅用于演示，实际数据应通过 ETL 从医药魔方同步';
END $$;
