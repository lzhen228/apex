package com.harbourbiomed.apex;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Apex Platform 主启动类
 * 
 * 早期靶点情报分析智能体后端应用入口
 * 
 * @author Harbour BioMed
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.harbourbiomed.apex")
@MapperScan({
        "com.harbourbiomed.apex.common.mapper",
        "com.harbourbiomed.apex.datasync.mapper",
        "com.harbourbiomed.apex.competition.mapper",
        "com.harbourbiomed.apex.progress.mapper",
        "com.harbourbiomed.apex.filterpreset.mapper"
})
public class ApexApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApexApplication.class, args);
    }
}
