package com.harbourbiomed.apex;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.harbourbiomed.apex")
@MapperScan("com.harbourbiomed.apex.**.mapper")
public class ApexApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApexApplication.class, args);
    }
}
