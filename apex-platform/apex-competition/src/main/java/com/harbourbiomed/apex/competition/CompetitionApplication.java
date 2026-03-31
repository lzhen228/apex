package com.harbourbiomed.apex.competition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 靶点组合竞争格局模块启动类
 *
 * @author Harbour BioMed
 */
@SpringBootApplication(scanBasePackages = "com.harbourbiomed.apex")
public class CompetitionApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompetitionApplication.class, args);
    }
}
