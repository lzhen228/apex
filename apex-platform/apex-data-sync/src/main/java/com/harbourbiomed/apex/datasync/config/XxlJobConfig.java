package com.harbourbiomed.apex.datasync.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * XXL-Job 配置类
 * 
 * @author Harbour BioMed
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "xxl.job", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(XxlJobConfig.XxlJobProperties.class)
public class XxlJobConfig {

    private final XxlJobProperties xxlJobProperties;

    /**
     * XXL-Job 执行器配置
     * 
     * @return XxlJobSpringExecutor 实例
     */
    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info("初始化 XXL-Job 执行器");
        
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(xxlJobProperties.getAdmin().getAddresses());
        executor.setAppname(xxlJobProperties.getExecutor().getAppname());
        executor.setAddress(xxlJobProperties.getExecutor().getAddress());
        executor.setIp(xxlJobProperties.getExecutor().getIp());
        executor.setPort(xxlJobProperties.getExecutor().getPort());
        executor.setAccessToken(resolveAccessToken());
        executor.setLogPath(xxlJobProperties.getExecutor().getLogPath());
        executor.setLogRetentionDays(xxlJobProperties.getExecutor().getLogRetentionDays());
        
        log.info("XXL-Job 执行器配置完成: {}", xxlJobProperties);
        return executor;
    }

    private String resolveAccessToken() {
        if (xxlJobProperties.getAccessToken() != null && !xxlJobProperties.getAccessToken().isBlank()) {
            return xxlJobProperties.getAccessToken();
        }
        return xxlJobProperties.getExecutor().getAccessToken();
    }

    /**
     * XXL-Job 配置属性
     */
    @lombok.Data
    @org.springframework.boot.context.properties.ConfigurationProperties(prefix = "xxl.job")
    public static class XxlJobProperties {
        /**
         * 调度中心配置
         */
        private Admin admin = new Admin();

        /**
         * 执行器全局通讯令牌
         */
        private String accessToken;

        /**
         * 执行器配置
         */
        private Executor executor = new Executor();
    }

    @lombok.Data
    public static class Admin {
        /**
         * 调度中心部署地址
         */
        private String addresses;
    }

    @lombok.Data
    public static class Executor {
        /**
         * 执行器 AppName
         */
        private String appname = "apex-data-sync";

        /**
         * 执行器注册地址
         */
        private String address;

        /**
         * 执行器 IP
         */
        private String ip;

        /**
         * 执行器端口
         */
        private int port = 9999;

        /**
         * 执行器通讯令牌
         */
        private String accessToken;

        /**
         * 执行器日志路径
         */
        private String logPath = "/data/applogs/xxl-job/jobhandler";

        /**
         * 执行器日志保留天数
         */
        private int logRetentionDays = 30;
    }
}
