package com.fzolv.shareware.core.exceptions;

public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super("BAD_REQUEST", 400, message);
    }
}


