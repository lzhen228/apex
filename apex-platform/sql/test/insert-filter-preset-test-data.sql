-- 筛选条件预设测试数据
-- 插入一些示例预设数据用于测试

-- 插入预设数据（假设已有用户ID=1）
INSERT INTO filter_preset (user_id, name, module, conditions, is_default, created_at, updated_at)
VALUES
    (
        1,
        'TNF 靶点筛选',
        'competition',
        '{
            "diseaseIds": [203, 204],
            "targets": ["TNF"],
            "phases": ["Approved", "Phase III"],
            "sortBy": "scorePhase DESC"
        }'::jsonb,
        true,
        NOW(),
        NOW()
    ),
    (
        1,
        'IL-6 靶点进展',
        'progress',
        '{
            "diseaseId": 203,
            "targets": ["IL-6", "TNF"],
            "phases": ["Approved", "Phase III"],
            "origins": ["AbbVie", "Novartisis"],
            "sortBy": "targetName",
            "sortOrder": "DESC"
        }'::jsonb,
        false,
        NOW(),
        NOW()
    ),
    (
        1,
        '多靶点竞争分析',
        'competition',
        '{
            "diseaseIds": [203, 204, 205],
            "targets": ["TNF", "IL-6", "EGFR"],
            "phases": ["Approved", "Phase III", "Phase II"],
            "origins": ["AbbVie", "Novartis", "Pfizer"],
            "sortBy": "scorePhase DESC",
            "scorePhaseMin": 2.0,
            "scorePhaseMax": 4.0
        }'::jsonb,
        false,
        NOW(),
        NOW()
    );

-- 查询插入的数据
SELECT 
    id,
    user_id,
    name,
    module,
    conditions,
    is_default,
    created_at,
    updated_at
FROM filter_preset
ORDER BY created_at DESC;

COMMENT ON SCRIPT '插入筛选条件预设测试数据';
