package com.harbourbiomed.apex.datasync.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 研发阶段归一化服务
 * <p>
 * 将 Parquet 中 indication_top_global_latest_stage 字段的多种写法
 * 统一映射到标准阶段名称及对应分值，具体规则见 TECH_SPEC 附录 B。
 */
@Service
public class PhaseNormalizationService {

    /** 标准阶段名称 → 分值 */
    private static final Map<String, BigDecimal> SCORE_MAP = Map.of(
            "Approved",    new BigDecimal("4.0"),
            "BLA",         new BigDecimal("3.5"),
            "Phase III",   new BigDecimal("3.0"),
            "Phase II/III",new BigDecimal("2.5"),
            "Phase II",    new BigDecimal("2.0"),
            "Phase I/II",  new BigDecimal("1.5"),
            "Phase I",     new BigDecimal("1.0"),
            "IND",         new BigDecimal("0.5"),
            "PreClinical", new BigDecimal("0.1")
    );

    /**
     * 将原始阶段字符串归一化为标准名称。
     * 匹配策略：先精确（忽略大小写），再前缀模糊，兜底返回原值。
     *
     * @param rawPhase Parquet 中的原始阶段字符串，可能为 null
     * @return 标准阶段名称；若无法识别则返回原值（不阻断 ETL 流程）
     */
    public String normalize(String rawPhase) {
        if (rawPhase == null || rawPhase.isBlank()) {
            return null;
        }
        String trimmed = rawPhase.trim();
        String lower = trimmed.toLowerCase();

        // ── 中文阶段名称 ──────────────────────────────────────────
        if (trimmed.equals("批准上市")) return "Approved";
        if (trimmed.equals("申请上市")) return "BLA";
        if (trimmed.equals("III期临床")) return "Phase III";
        if (trimmed.equals("II/III期临床")) return "Phase II/III";
        if (trimmed.equals("II期临床")) return "Phase II";
        if (trimmed.equals("I/II期临床")) return "Phase I/II";
        if (trimmed.equals("I期临床")) return "Phase I";
        if (trimmed.equals("申报临床")) return "IND";
        if (trimmed.equals("临床前")) return "PreClinical";

        // ── Approved ──────────────────────────────────────────────
        if (lower.equals("approved") || lower.equals("market")
                || lower.equals("marketed") || lower.startsWith("approved")) {
            return "Approved";
        }
        // ── BLA / NDA / ANDA ──────────────────────────────────────
        if (lower.equals("bla") || lower.equals("nda") || lower.equals("anda")
                || lower.equals("snda") || lower.equals("sbla")
                || lower.startsWith("bla") || lower.startsWith("nda")) {
            return "BLA";
        }
        // ── Phase II/III ──────────────────────────────────────────
        if (lower.contains("2/3") || lower.contains("ii/iii")
                || lower.contains("phase 2/3") || lower.contains("phase ii/iii")) {
            return "Phase II/III";
        }
        // ── Phase III ─────────────────────────────────────────────
        if (lower.contains("phase 3") || lower.contains("phase iii")
                || lower.equals("p3") || lower.startsWith("phase 3")) {
            return "Phase III";
        }
        // ── Phase I/II ────────────────────────────────────────────
        if (lower.contains("1/2") || lower.contains("i/ii")
                || lower.contains("phase 1/2") || lower.contains("phase i/ii")) {
            return "Phase I/II";
        }
        // ── Phase II ──────────────────────────────────────────────
        if (lower.contains("phase 2") || lower.contains("phase ii")
                || lower.equals("p2")) {
            return "Phase II";
        }
        // ── Phase I ───────────────────────────────────────────────
        if (lower.contains("phase 1") || lower.contains("phase i")
                || lower.equals("p1")) {
            return "Phase I";
        }
        // ── IND ───────────────────────────────────────────────────
        if (lower.startsWith("ind") || lower.startsWith("cta")) {
            return "IND";
        }
        // ── PreClinical ───────────────────────────────────────────
        if (lower.startsWith("preclinical") || lower.startsWith("pre-clinical")
                || lower.startsWith("discovery") || lower.startsWith("lead")) {
            return "PreClinical";
        }

        // 无法识别时原样保留，不阻断 ETL
        return trimmed;
    }

    /**
     * 根据标准阶段名称返回对应分值。
     *
     * @return 阶段分值；若阶段名称未在映射表中则返回 null
     */
    public BigDecimal getScore(String standardPhase) {
        if (standardPhase == null) {
            return null;
        }
        return SCORE_MAP.get(standardPhase);
    }
}
