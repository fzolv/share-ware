package com.fzolv.shareware.hull.strategy.impl;

import com.fzolv.shareware.data.entities.ExpenseEntity;
import com.fzolv.shareware.data.entities.ExpenseSplitEntity;
import com.fzolv.shareware.data.entities.UserEntity;
import com.fzolv.shareware.data.repositories.UserRepository;
import com.fzolv.shareware.hull.models.requests.ExpenseRequest;
import com.fzolv.shareware.hull.strategy.ExpenseSplitStrategy;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class ExactSplitStrategy implements ExpenseSplitStrategy {

    @Override
    public List<ExpenseSplitEntity> calculateSplits(ExpenseEntity expense, List<ExpenseRequest.SplitRequest> splitRequests, UserRepository userRepository) {
        if (splitRequests == null || splitRequests.isEmpty()) {
            throw new IllegalArgumentException("Exact split requires explicit split requests with amounts");
        }

        // Validate all users are members of the expense group
        Set<UUID> groupUserIds = new HashSet<>();
        expense.getGroup().getMembers().forEach(m -> groupUserIds.add(m.getUser().getId()));

        double sum = 0.0;
        List<ExpenseSplitEntity> splits = new ArrayList<>();
        for (ExpenseRequest.SplitRequest s : splitRequests) {
            if (s.getAmount() == null) {
                throw new IllegalArgumentException("Exact split requires amount for user: " + s.getUserId());
            }
            UUID uid = UUID.fromString(s.getUserId());
            if (!groupUserIds.contains(uid)) {
                throw new IllegalArgumentException("User " + s.getUserId() + " is not a member of the group");
            }
            sum += s.getAmount();
            UserEntity user = userRepository.findById(uid)
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + s.getUserId()));
            ExpenseSplitEntity split = new ExpenseSplitEntity();
            split.setExpense(expense);
            split.setUser(user);
            split.setAmountOwed(round2(s.getAmount()));
            split.setPercentage(null);
            splits.add(split);
        }

        double total = round2(expense.getAmount());
        if (Math.abs(round2(sum) - total) > 0.01) {
            throw new IllegalArgumentException("Exact split amounts (" + sum + ") must equal expense total (" + total + ")");
        }
        return splits;
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}


