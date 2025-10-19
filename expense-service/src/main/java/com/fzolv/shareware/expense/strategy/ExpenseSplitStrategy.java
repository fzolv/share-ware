package com.fzolv.shareware.expense.strategy;

import com.fzolv.shareware.data.entities.ExpenseEntity;
import com.fzolv.shareware.data.entities.ExpenseSplitEntity;
import com.fzolv.shareware.data.repositories.UserRepository;
import com.fzolv.shareware.expense.models.requests.ExpenseRequest;

import java.util.List;

public interface ExpenseSplitStrategy {
    List<ExpenseSplitEntity> calculateSplits(ExpenseEntity expense, List<ExpenseRequest.SplitRequest> splitRequests, UserRepository userRepository);
}
