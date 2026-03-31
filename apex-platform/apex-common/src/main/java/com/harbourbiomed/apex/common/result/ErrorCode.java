package com.harbourbiomed.apex.common.result;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // 认证
    AUTH_CREDENTIALS_INVALID(1001, "用户名或密码错误"),
    AUTH_TOKEN_EXPIRED(1002, "Token 已过期"),
    AUTH_UNAUTHORIZED(1003, "无权限"),
    AUTH_USER_DISABLED(1004, "用户已被禁用"),
    AUTH_FAILED(1005, "认证失败"),

    // 竞争格局
    COMPETITION_DISEASE_INVALID(2001, "疾病参数无效"),
    COMPETITION_MATRIX_EMPTY(2002, "矩阵数据为空"),

    // 研发进展
    PROGRESS_DISEASE_REQUIRED(3001, "疾病必选"),
    PROGRESS_TARGET_INVALID(3002, "靶点参数无效"),

    // 通用
    PARAM_INVALID(9001, "参数校验失败"),
    SYSTEM_ERROR(9002, "系统内部错误"),
    RATE_LIMIT(9003, "请求频率过高");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
