package com.harbourbiotech.apex.common.exception;

import com.harbourbiotech.apex.common.model.ErrorCode;
import lombok.Getter;

/**
 * 业务异常类
 * <p>
 * 用于封装业务逻辑中的异常情况，统一错误码和错误信息
 *
 * @author Harbour BioMed
 * @version 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 创建业务异常（使用错误码枚举）
     *
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 创建业务异常（使用错误码枚举和自定义消息）
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误信息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    /**
     * 创建业务异常（自定义错误码和消息）
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 创建业务异常（仅自定义消息，使用默认错误码）
     *
     * @param message 错误信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 5000; // 默认业务异常错误码
    }

    /**
     * 创建业务异常（使用错误码枚举和原始异常）
     *
     * @param errorCode 错误码枚举
     * @param cause     原始异常
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
    }

    /**
     * 创建业务异常（使用错误码枚举、自定义消息和原始异常）
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误信息
     * @param cause     原始异常
     */
    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.code = errorCode.getCode();
    }
}
