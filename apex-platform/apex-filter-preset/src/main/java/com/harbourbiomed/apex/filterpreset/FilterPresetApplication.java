package com.harbourbiomed.apex.filterpreset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 筛选条件预设模块启动类
 * 
 * @author Harbour BioMed
 */
@SpringBootApplication(scanBasePackages = "com.harbourbiomed.apex")
public class FilterPresetApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilterPresetApplication.class, args);
    }
}
