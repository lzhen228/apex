package com.harbourbiomed.apex.progress;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Apex 进展模块启动类
 *
 * 靶点研发进展格局模块 - 管线全景图
 *
 * @author Harbour BioMed
 */
@EnableCaching
@SpringBootApplication(scanBasePackages = "com.harbourbiomed.apex")
public class ProgressApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProgressApplication.class, args);
    }
}
