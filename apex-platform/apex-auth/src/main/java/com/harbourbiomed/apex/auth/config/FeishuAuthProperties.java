package com.harbourbiomed.apex.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "apex.feishu")
public class FeishuAuthProperties {

    private boolean enabled;
    private String appId;
    private String appSecret;
    private String redirectUri;
    private String authorizeUrl = "https://open.feishu.cn/connect/qrconnect/page/sso";
    private String accessTokenUrl = "https://open.feishu.cn/open-apis/authen/v1/access_token";
}