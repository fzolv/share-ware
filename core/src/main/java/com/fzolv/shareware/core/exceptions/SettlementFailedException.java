package com.fzolv.shareware.core.exceptions;

public class SettlementFailedException extends ApiException {
    public SettlementFailedException(String reason) {
        super("SETTLEMENT_FAILED", 400, "Settlement failed: " + reason);
    }
}
