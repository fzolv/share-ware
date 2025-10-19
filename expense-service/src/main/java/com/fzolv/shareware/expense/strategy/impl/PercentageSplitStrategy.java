package com.fzolv.shareware.expense.strategy.impl;

import com.fzolv.shareware.data.entities.ExpenseEntity;
import com.fzolv.shareware.data.entities.ExpenseSplitEntity;
import com.fzolv.shareware.data.entities.UserEntity;
import com.fzolv.shareware.data.repositories.UserRepository;
import com.fzolv.shareware.expense.models.requests.ExpenseRequest;
import com.fzolv.shareware.expense.strategy.ExpenseSplitStrategy;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class PercentageSplitStrategy implements ExpenseSplitStrategy {

    @Override
    public List<ExpenseSplitEntity> calculateSplits(ExpenseEntity expense, List<ExpenseRequest.SplitRequest> splitRequests, UserRepository userRepository) {
        if (splitRequests == null || splitRequests.isEmpty()) {
            throw new IllegalArgumentException("Percentage splits require explicit splitRequests with percentages");
        }
        List<ExpenseSplitEntity> splits = new ArrayList<>();
        for (ExpenseRequest.SplitRequest s : splitRequests) {
            UserEntity user = userRepository.findById(UUID.fromString(s.getUserId()))
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + s.getUserId()));
            ExpenseSplitEntity split = new ExpenseSplitEntity();
            split.setExpense(expense);
            split.setUser(user);
            split.setPercentage(s.getPercentage());
            double amount = Math.round((expense.getAmount() * (s.getPercentage() / 100.0)) * 100.0) / 100.0;
            split.setAmountOwed(amount);
            splits.add(split);
        }
        return splits;
    }
}
