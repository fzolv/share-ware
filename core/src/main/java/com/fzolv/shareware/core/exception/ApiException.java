package com.fzolv.shareware.core.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final String errorCode;
    private final int httpCode;
    private final String message;

    public ApiException(String errorCode, int httpCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpCode = httpCode;
        this.message = message;
    }
}
