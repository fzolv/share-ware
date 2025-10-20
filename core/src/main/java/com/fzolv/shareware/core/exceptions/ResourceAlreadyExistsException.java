package com.fzolv.shareware.core.exceptions;


public class ResourceAlreadyExistsException extends ApiException {
    public ResourceAlreadyExistsException(String message) {
        super("BAD_REQUEST", 400, message);
    }
}