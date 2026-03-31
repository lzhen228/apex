package com.harbourbiotech.apex.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 统一响应封装类
 * <p>
 * 所有API接口返回统一格式，包含状态码、消息、数据和时间戳
 *
 * @param <T> 响应数据类型
 * @author Harbour BioMed
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 业务状态码
     * 0 = 成功
     * 非0 = 失败（4位业务错误码）
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 服务端时间戳（毫秒）
     */
    private Long timestamp;

    /**
     *. 创建成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success() {
        return Result.<T>builder()
                .code(0)
                .message("success")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(0)
                .message("success")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建成功响应（带消息和数据）
     *
     * @param message 提示信息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder()
                .code(0)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建错误响应（仅消息）
     *
     * @param message 错误信息
     * @param <T>     数据类型
     * @return 错误响应对象
     */
    public static <T> Result<T> error(String message) {
        return Result.<T>builder()
                .code(9002) // 默认系统错误
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建错误响应（自定义错误码和消息）
     *
     * @param code    错误码
     * @param message 错误信息
     * @param <T>     数据类型
     * @return 错误响应对象
     */
    public static <T> Result<T> error(Integer code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 根据错误码枚举创建错误响应
     *
     * @param errorCode 错误码枚举
     * @param <T>       数据类型
     * @return 错误响应对象
     */
    public static <T> Result<T> error(ErrorCode errorCode) {
        return Result.<T>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 根据错误码枚举创建错误响应（自定义消息）
     *
     * @param errorCode 错误码枚举
     * @param message   `自定义错误信息
     * @param <T>       数据类型
     * @return 错误响应对象
     */
    public static <T> Result<T> error(ErrorCode errorCode, String message) {
        return Result.<T>builder()
                .code(errorCode.getCode())
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
