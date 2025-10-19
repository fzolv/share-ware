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
public class EqualSplitStrategy implements ExpenseSplitStrategy {

    @Override
    public List<ExpenseSplitEntity> calculateSplits(ExpenseEntity expense, List<ExpenseRequest.SplitRequest> splitRequests, UserRepository userRepository) {
        List<ExpenseSplitEntity> splits = new ArrayList<>();
        // If explicit splitRequests provided, distribute among those users equally
        if (splitRequests != null && !splitRequests.isEmpty()) {
            int participants = splitRequests.size();
            double perPerson = Math.round((expense.getAmount() / participants) * 100.0) / 100.0;
            for (ExpenseRequest.SplitRequest s : splitRequests) {
                UserEntity user = userRepository.findById(UUID.fromString(s.getUserId()))
                        .orElseThrow(() -> new EntityNotFoundException("User not found: " + s.getUserId()));
                ExpenseSplitEntity split = new ExpenseSplitEntity();
                split.setExpense(expense);
                split.setUser(user);
                split.setAmountOwed(perPerson);
                split.setPercentage(null);
                splits.add(split);
            }
            return splits;
        }

        // Otherwise, derive participants from the group's members
        expense.getGroup().getMembers().forEach(m -> {
            ExpenseSplitEntity split = new ExpenseSplitEntity();
            split.setExpense(expense);
            split.setUser(m.getUser());
            split.setAmountOwed(Math.round((expense.getAmount() / expense.getGroup().getMembers().size()) * 100.0) / 100.0);
            split.setPercentage(null);
            splits.add(split);
        });

        return splits;
    }
}
