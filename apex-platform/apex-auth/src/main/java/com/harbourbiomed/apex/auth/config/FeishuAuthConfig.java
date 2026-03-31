package com.harbourbiomed.apex.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FeishuAuthProperties.class)
public class FeishuAuthConfig {
}