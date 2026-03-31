package com.harbourbiomed.apex.common.exception;

import lombok.Getter;

/**
 * Token 过期异常
 *
 * @author Harbour BioMed
 */
@Getter
public class TokenExpiredException extends RuntimeException {

    private Integer code;

    public TokenExpiredException(String message) {
        super(message);
        this.code = 1002;
    }

    public TokenExpiredException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
        this.code = 1002;
    }

    public TokenExpiredException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
