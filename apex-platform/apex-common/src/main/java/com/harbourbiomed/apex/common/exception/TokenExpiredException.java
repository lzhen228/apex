package com.harbourbiomed.apex.common.exception;

import lombok.Getter;

@Getter
public class TokenExpiredException extends RuntimeException {

    private final int code = 1002;

    public TokenExpiredException(String message) {
        super(message);
    }
}
