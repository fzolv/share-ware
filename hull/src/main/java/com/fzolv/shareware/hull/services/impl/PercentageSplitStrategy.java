package com.fzolv.shareware.hull.services.impl;

import com.fzolv.shareware.data.entities.ExpenseEntity;
import com.fzolv.shareware.data.entities.ExpenseSplitEntity;
import com.fzolv.shareware.data.repositories.UserRepository;
import com.fzolv.shareware.hull.models.requests.ExpenseRequest;
import com.fzolv.shareware.hull.strategy.ExpenseSplitStrategy;

import java.util.List;

public class PercentageSplitStrategy implements ExpenseSplitStrategy {
    @Override
    public List<ExpenseSplitEntity> calculateSplits(ExpenseEntity expense, List<ExpenseRequest.SplitRequest> splitRequests, UserRepository userRepository) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
