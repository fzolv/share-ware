package com.fzolv.shareware.core.exception;

public class SettlementFailedException extends ApiException {
    public SettlementFailedException(String reason) {
        super("SETTLEMENT_FAILED", 400, "Settlement failed: " + reason);
    }
}
