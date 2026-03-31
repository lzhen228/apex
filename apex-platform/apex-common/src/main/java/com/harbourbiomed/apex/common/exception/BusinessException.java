package com.harbourbiomed.apex.common.exception;

import com.harbourbiomed.apex.common.result.ErrorCode;
import lombok.Getter;

/**
 * 业务异常类
 * 
 * @author Harbour BioMed
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Integer code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = 4001; // 默认业务错误码
    }
}
