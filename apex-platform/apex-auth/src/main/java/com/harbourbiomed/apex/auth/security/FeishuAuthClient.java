package com.harbourbiomed.apex.auth.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.harbourbiomed.apex.auth.config.FeishuAuthProperties;
import com.harbourbiomed.apex.common.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeishuAuthClient {

    private final RestClient.Builder restClientBuilder;
    private final FeishuAuthProperties properties;

    public FeishuUserProfile exchangeCode(String code) {
        validateConfig();

        FeishuAccessTokenResponse response = restClientBuilder.build()
                .post()
                .uri(properties.getAccessTokenUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new FeishuAccessTokenRequest(
                        properties.getAppId(),
                        properties.getAppSecret(),
                        "authorization_code",
                        code))
                .retrieve()
                .body(FeishuAccessTokenResponse.class);

        if (response == null) {
            throw new AuthenticationException("飞书登录失败：响应为空");
        }
        if (response.getCode() != 0 || response.getData() == null) {
            String message = StringUtils.hasText(response.getMsg()) ? response.getMsg() : response.getMessage();
            log.warn("飞书换码失败 code={} message={}", response.getCode(), message);
            throw new AuthenticationException(StringUtils.hasText(message) ? message : "飞书登录失败");
        }

        FeishuAccessTokenData data = response.getData();
        return new FeishuUserProfile(
                firstNonBlank(data.getUnionId(), data.getOpenId()),
                data.getOpenId(),
                firstNonBlank(data.getName(), data.getEnName()));
    }

    private void validateConfig() {
        if (!properties.isEnabled()) {
            throw new AuthenticationException("飞书登录未启用");
        }
        if (!StringUtils.hasText(properties.getAppId()) || !StringUtils.hasText(properties.getAppSecret())) {
            throw new AuthenticationException("飞书登录配置不完整，请检查 APEX_FEISHU_APP_ID / APEX_FEISHU_APP_SECRET");
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private record FeishuAccessTokenRequest(
            @JsonProperty("app_id") String appId,
            @JsonProperty("app_secret") String appSecret,
            @JsonProperty("grant_type") String grantType,
            String code) {
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FeishuAccessTokenResponse {
        private int code;
        private String msg;
        private String message;
        private FeishuAccessTokenData data;
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FeishuAccessTokenData {
        @JsonProperty("open_id")
        private String openId;

        @JsonProperty("union_id")
        private String unionId;

        private String name;

        @JsonProperty("en_name")
        private String enName;
    }

    public record FeishuUserProfile(String unionId, String openId, String displayName) {
    }
}