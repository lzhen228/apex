package com.harbourbiomed.apex.common.exception;

import lombok.Getter;

/**
 * 认证异常
 *
 * @author Harbour BioMed
 */
@Getter
public class AuthenticationException extends RuntimeException {

    private Integer code;

    public AuthenticationException(String message) {
        super(message);
        this.code = 1001;
    }

    public AuthenticationException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.code = 1001;
    }

    public AuthenticationException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
