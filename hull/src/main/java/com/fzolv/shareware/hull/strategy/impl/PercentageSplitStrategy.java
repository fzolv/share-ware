package com.fzolv.shareware.hull.strategy.impl;

import com.fzolv.shareware.core.exceptions.BadRequestException;
import com.fzolv.shareware.data.entities.ExpenseEntity;
import com.fzolv.shareware.data.entities.ExpenseSplitEntity;
import com.fzolv.shareware.data.entities.UserEntity;
import com.fzolv.shareware.data.repositories.UserRepository;
import com.fzolv.shareware.hull.models.requests.ExpenseRequest;
import com.fzolv.shareware.hull.strategy.ExpenseSplitStrategy;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PercentageSplitStrategy implements ExpenseSplitStrategy {

    @Override
    public List<ExpenseSplitEntity> calculateSplits(ExpenseEntity expense, List<ExpenseRequest.SplitRequest> splitRequests, UserRepository userRepository) {
        if (splitRequests == null || splitRequests.isEmpty()) {
            throw new BadRequestException("Percentage splits require explicit splitRequests with percentages");
        }
        // Validate all users are members of the expense group
        Set<UUID> groupUserIds = new HashSet<>();
        expense.getGroup().getMembers().forEach(m -> groupUserIds.add(m.getUser().getId()));

        List<ExpenseSplitEntity> splits = new ArrayList<>();
        for (ExpenseRequest.SplitRequest s : splitRequests) {
            UUID uid = UUID.fromString(s.getUserId());
            if (!groupUserIds.contains(uid)) {
                throw new BadRequestException("User " + s.getUserId() + " is not a member of the group");
            }
            UserEntity user = userRepository.findById(uid)
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
