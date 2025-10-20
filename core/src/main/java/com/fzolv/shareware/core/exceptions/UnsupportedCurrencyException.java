package com.fzolv.shareware.core.exceptions;

public class UnsupportedCurrencyException extends ApiException {
    public UnsupportedCurrencyException(String currency) {
        super("UNSUPPORTED_CURRENCY", 400, "Unsupported currency: " + currency);
    }
}
