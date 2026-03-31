package com.harbourbiomed.apex.common.exception;

import lombok.Getter;

@Getter
public class AuthenticationException extends RuntimeException {

    private final int code;

    public AuthenticationException(String message) {
        super(message);
        this.code = 1001;
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.code = 1001;
    }
}
