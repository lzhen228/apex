package com.harbourbiomed.apex.auth.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private long expiresIn;

    private UserVO user;

    @Data
    @Builder
    public static class UserVO {
        private Long id;
        private String username;
        private String displayName;
    }
}
