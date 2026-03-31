package com.harbourbiotech.apex.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举定义
 * <p>
 * 错误码格式：XYYY
 * X = 模块标识（1=认证, 2=竞争格局, 3=研发进展, 4=数据同步, 9=通用）
 * YYY = 模块内序号（001-999）
 *
 * @author Harbour BioMed
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ========== 系统错误 (1000-1099) ==========
    /**
     * 系统异常
     */
    SYSTEM_ERROR(1000, "系统异常，请稍后重试"),
    /**
     * 参数未传入
     */
    PARAM_NOT_PROVIDED(1001, "参数未传入"),
    /**
     * 参数格式错误
     */
    PARAM_FORMAT_ERROR(1002, "参数格式错误"),
    /**
     * 参数值非法
     */
    PARAM_VALUE_INVALID(1003, "参数值非法"),
    /**
     * 参数冲突
     */
    PARAM_CONFLICT(1004, "参数冲突"),
    /**
     * 参数超出范围
     */
    PARAM_OUT_OF_RANGE(1005, "参数超出范围"),

    // ========== 认证相关 (2000-2099) ==========
    /**
     * 认证失败
     */
    AUTH_FAILED(2000, "认证失败"),
    /**
     * 用户不存在
     */
    USER_NOT_FOUND(2001, "用户不存在"),
    /**
     * 密码错误
     */
    PASSWORD_ERROR(2002, "密码错误"),
    /**
     * Token 过期
     */
    TOKEN_EXPIRED(2003, "Token 已过期"),
    /**
     * Token 无效
     */
    TOKEN_INVALID(2004, "Token 无效"),
    /**
     * Token 缺失
     */
    TOKEN_MISSING(2005, "Token 缺失"),

    // ========== 授权相关 (3000-3099) ==========
    /**
     * 未授权
     */
    UNAUTHORIZED(3000, "未授权"),
    /**
     * 无访问权限
     */
    ACCESS_DENIED(3001, "无访问权限"),

    // ========== 资源相关 (4000-4099) ==========
    /**
     * 资源不存在
     */
    RESOURCE_NOT_FOUND(4000, "资源不存在"),
    /**
     * 资源已被删除
     */
    RESOURCE_DELETED(4001, "资源已被删除"),

    // ========== 业务异常 (5000-5099) ==========
    /**
     * 业务异常
     */
    BUSINESS_ERROR(5000, "业务异常"),
    /**
     * 数据同步失败
     */
    DATA_SYNC_FAILED(5001, "数据同步失败"),
    /**
     * 数据不存在
     */
    DATA_NOT_FOUND(5002, "数据不存在"),
    /**
     * 数据已存在
     */
    DATA_ALREADY_EXISTS(5003, "数据已存在"),
    /**
     * 状态不允许该操作
     */
    STATE_NOT_ALLOW_OPERATION(5004, "当前状态不允许该操作"),

    // ========== 竞争格局模块 (2001-2099) ==========
    /**
     * 疾病参数无效
     */
    DISEASE_PARAM_INVALID(2101, "疾病参数无效"),
    /**
     * 矩阵数据为空
     */
    MATRIX_DATA_EMPTY(2102, "矩阵数据为空"),

    // ========== 研发进展模块 (3001-3099) ==========
    /**
     * 疾病必选
     */
    DISEASE_REQUIRED(3101, "疾病必选"),
    /**
     * 靶点参数无效
     */
    TARGET_PARAM_INVALID(3102, "靶点参数无效"),
    /**
     * 管线数据为空
     */
    PIPELINE_DATA_EMPTY(3103, "管线数据为空");

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;
}
