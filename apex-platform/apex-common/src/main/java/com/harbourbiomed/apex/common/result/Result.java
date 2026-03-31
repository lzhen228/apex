package com.harbourbiomed.apex.common.result;

import lombok.Data;

import java.util.UUID;

@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;
    private String traceId;
    private long timestamp;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = 0;
        r.message = "success";
        r.data = data;
        r.traceId = UUID.randomUUID().toString();
        r.timestamp = System.currentTimeMillis();
        return r;
    }

    public static <T> Result<T> ok(String message, T data) {
        Result<T> r = ok(data);
        r.message = message;
        return r;
    }

    public static <Void> Result<Void> fail(int code, String message) {
        Result<Void> r = new Result<>();
        r.code = code;
        r.message = message;
        r.traceId = UUID.randomUUID().toString();
        r.timestamp = System.currentTimeMillis();
        return r;
    }

    public static <Void> Result<Void> fail(ErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMessage());
    }
}
