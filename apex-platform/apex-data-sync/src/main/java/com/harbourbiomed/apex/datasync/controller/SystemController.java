package com.harbourbiomed.apex.datasync.controller;

import com.harbourbiomed.apex.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Tag(name = "系统信息")
@RestController
@RequestMapping("/v1/system")
@RequiredArgsConstructor
public class SystemController {

    private final JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Operation(summary = "最近一次数据同步成功时间")
    @GetMapping("/last-sync")
    public Result<Map<String, String>> lastSync() {
        String sql = """
                SELECT end_time FROM data_sync_log
                WHERE module = 'ci_tracking' AND status = 'success'
                ORDER BY end_time DESC LIMIT 1
                """;

        String time;
        try {
            time = jdbcTemplate.queryForObject(sql, (rs, n) -> {
                var ts = rs.getTimestamp("end_time");
                return ts != null ? ts.toLocalDateTime().format(FORMATTER) : null;
            });
        } catch (Exception e) {
            time = null;
        }

        if (time == null) {
            // 默认：今天 05:00
            time = LocalDate.now().atTime(5, 0).format(FORMATTER);
        }

        return Result.ok(Map.of("updatedAt", time));
    }
}
