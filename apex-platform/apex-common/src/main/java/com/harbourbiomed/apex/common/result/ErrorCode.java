package com.harbourbiomed.apex.common.result;

import lombok.Getter;

/**
 * 业务错误码枚举
 * 
 * 错误码规则：4 位业务码
 * - 1xxx: 认证授权相关
 * - 2xxx: 用户相关
 * - 3xxx: 数据相关
 * - 4xxx: 业务逻辑相关
 * - 9xxx: 系统错误
 * 
 * @author Harbour BioMed
 */
@Getter
public enum ErrorCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 认证授权相关 1xxx
    AUTH_TOKEN_INVALID(1001, "Token 无效"),
    AUTH_TOKEN_EXPIRED(1002, "Token 已过期"),
    AUTH_TOKEN_MISSING(1003, "Token 缺失"),
    AUTH_LOGIN_FAILED(1004, "登录失败，用户名或密码错误"),
    AUTH_UNAUTHORIZED(1005, "未授权访问"),

    // 用户相关 2xxx
    USER_NOT_FOUND(2001, "用户不存在"),
    USER_DISABLED(2002, "用户已被禁用"),

    // 数据相关 3xxx
    DATA_NOT_FOUND(3001, "数据不存在"),
    DATA_DUPLICATE(3002, "数据重复"),
    DATA_INVALID(3003, "数据格式无效"),

    // 业务逻辑相关 4xxx
    BUSINESS_ERROR(4001, "业务处理失败"),
    PARAM_INVALID(4002, "参数校验失败"),

    // 系统错误 9xxx
    SYSTEM_ERROR(9001, "系统内部错误"),
    NETWORK_ERROR(9002, "网络错误");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
