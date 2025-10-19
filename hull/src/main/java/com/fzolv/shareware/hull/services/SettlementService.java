package com.fzolv.shareware.hull.services;

import com.fzolv.shareware.hull.models.requests.SettlementRequest;

public interface SettlementService {
    void settleExpense(String expenseId, SettlementRequest request);
}
