package com.fzolv.shareware.expense.services.impl;

import com.fzolv.shareware.expense.strategy.ExpenseSplitStrategy;

public class PercentageSplitStrategy implements ExpenseSplitStrategy {
    @Override
    public java.util.List<com.fzolv.shareware.data.entities.ExpenseSplitEntity> calculateSplits(com.fzolv.shareware.data.entities.ExpenseEntity expense, java.util.List<com.fzolv.shareware.expense.models.requests.ExpenseRequest.SplitRequest> splitRequests, com.fzolv.shareware.data.repositories.UserRepository userRepository) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
