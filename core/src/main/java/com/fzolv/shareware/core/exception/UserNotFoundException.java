package com.fzolv.shareware.core.exception;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException(String userId) {
        super("USER_NOT_FOUND", 404, "User not found: " + userId);
    }
}
