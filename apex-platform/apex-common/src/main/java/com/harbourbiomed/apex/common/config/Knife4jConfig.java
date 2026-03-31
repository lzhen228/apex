package com.harbourbiomed.apex.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j (Swagger) 配置类
 * 
 * @author Harbour BioMed
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Apex Platform API")
                        .version("1.0.0")
                        .description("和铂医药药物早期研发靶点情报分析平台 API 文档")
                        .contact(new Contact()
                                .name("Harbour BioMed")
                                .email("support@harbourbiomed.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
