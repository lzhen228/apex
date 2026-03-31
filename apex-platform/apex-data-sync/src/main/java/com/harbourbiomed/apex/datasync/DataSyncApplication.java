package com.harbourbiomed.apex.datasync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 数据同步模块应用启动类
 * 
 * @author Harbour BioMed
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.harbourbiomed.apex.datasync",
    "com.harbourbiomed.apex.common"
})
public class DataSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataSyncApplication.class, args);
    }
}
