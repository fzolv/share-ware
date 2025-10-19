package com.fzolv.shareware.hull.services.impl;

import com.fzolv.shareware.data.entities.ExpenseEntity;
import com.fzolv.shareware.data.entities.ExpenseSplitEntity;
import com.fzolv.shareware.data.entities.GroupEntity;
import com.fzolv.shareware.data.entities.UserEntity;
import com.fzolv.shareware.data.repositories.ExpenseRepository;
import com.fzolv.shareware.data.repositories.GroupRepository;
import com.fzolv.shareware.data.repositories.UserRepository;
import com.fzolv.shareware.hull.mapper.ExpenseMapper;
import com.fzolv.shareware.hull.models.dtos.ExpenseDto;
import com.fzolv.shareware.hull.models.requests.ExpenseRequest;
import com.fzolv.shareware.hull.services.ExpenseService;
import com.fzolv.shareware.hull.strategy.ExpenseSplitStrategy;
import com.fzolv.shareware.hull.strategy.impl.EqualSplitStrategy;
import com.fzolv.shareware.hull.events.EventPublisher;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fzolv.shareware.hull.locks.LockManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ExpenseMapper mapper;
    private final EqualSplitStrategy equalSplitStrategy; // default; could be wired by map of strategies
    private final EventPublisher eventPublisher;
    private final LockManager lockManager;

    @Override
    @Transactional
    public ExpenseDto createExpense(ExpenseRequest request) {
        GroupEntity group = groupRepository.findById(UUID.fromString(request.getGroupId()))
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));
        UserEntity paidBy = userRepository.findById(UUID.fromString(request.getPaidById()))
                .orElseThrow(() -> new EntityNotFoundException("Payer not found"));

        ExpenseEntity expense = new ExpenseEntity();
        expense.setGroup(group);
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setPaidBy(paidBy);
        expense.setCurrency(request.getCurrency());
        expense.setSplitType(com.fzolv.shareware.data.entities.SplitType.valueOf(request.getSplitType()));
        LocalDateTime now = LocalDateTime.now();
        expense.setCreatedAt(now);
        expense.setUpdatedAt(now);

        expense = expenseRepository.save(expense);

        // calculate splits using strategy
        ExpenseSplitStrategy strategy = resolveStrategy(expense.getSplitType());
        List<ExpenseSplitEntity> splits = strategy.calculateSplits(expense, request.getSplits(), userRepository);
        expense.setSplits(splits);
        expense = expenseRepository.save(expense);

        Map<String, Object> payload = new HashMap<>();
        payload.put("expenseId", expense.getId().toString());
        payload.put("groupId", expense.getGroup().getId().toString());
        payload.put("amount", expense.getAmount());
        Set<String> userIds = new HashSet<>();
        userIds.add(expense.getPaidBy().getId().toString());
        for (ExpenseSplitEntity s : splits) {
            if (s.getUser() != null && s.getUser().getId() != null) {
                userIds.add(s.getUser().getId().toString());
            }
        }
        payload.put("userIds", userIds);
        eventPublisher.publish("shareware.expense.events", "EXPENSE_CREATED", payload);

        return mapper.toDto(expense);
    }

    @Override
    @Transactional
    public ExpenseDto updateExpense(String expenseId, ExpenseRequest request) {
        String lockKey = "expense:" + expenseId;
        lockManager.lock(lockKey);
        try {
            ExpenseEntity expense = expenseRepository.findById(UUID.fromString(expenseId))
                    .orElseThrow(() -> new EntityNotFoundException("Expense not found"));

            expense.setDescription(request.getDescription());
            expense.setAmount(request.getAmount());
            expense.setCurrency(request.getCurrency());
            expense.setSplitType(com.fzolv.shareware.data.entities.SplitType.valueOf(request.getSplitType()));

            // Recalculate splits
            ExpenseSplitStrategy strategy = resolveStrategy(expense.getSplitType());
            List<ExpenseSplitEntity> splits = strategy.calculateSplits(expense, request.getSplits(), userRepository);
            expense.getSplits().clear();
            expense.getSplits().addAll(splits);

            expense.setUpdatedAt(LocalDateTime.now());
            expense = expenseRepository.save(expense);

            Map<String, Object> payload = new HashMap<>();
            payload.put("expenseId", expense.getId().toString());
            payload.put("groupId", expense.getGroup().getId().toString());
            Set<String> userIds = new HashSet<>();
            userIds.add(expense.getPaidBy().getId().toString());
            for (ExpenseSplitEntity s : expense.getSplits()) {
                if (s.getUser() != null && s.getUser().getId() != null) {
                    userIds.add(s.getUser().getId().toString());
                }
            }
            payload.put("userIds", userIds);
            eventPublisher.publish("shareware.expense.events", "EXPENSE_UPDATED", payload);
            return mapper.toDto(expense);
        } finally {
            lockManager.releaseLock(lockKey);
        }
    }

    @Override
    @Transactional
    public void deleteExpense(String expenseId) {
        String lockKey = "expense:" + expenseId;
        lockManager.lock(lockKey);
        try {
            if (!expenseRepository.existsById(UUID.fromString(expenseId))) {
                throw new EntityNotFoundException("Expense not found");
            }
            expenseRepository.deleteById(UUID.fromString(expenseId));
            Map<String, Object> payload = new HashMap<>();
            payload.put("expenseId", expenseId);
            // for delete, we can include the group and payer if needed (look up optional)
            eventPublisher.publish("shareware.expense.events", "EXPENSE_DELETED", payload);
        } finally {
            lockManager.releaseLock(lockKey);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDto getExpenseById(String expenseId) {
        return expenseRepository.findById(UUID.fromString(expenseId))
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseDto> getExpensesByGroupId(String groupId) {
        return expenseRepository.findByGroupId(UUID.fromString(groupId)).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    private ExpenseSplitStrategy resolveStrategy(com.fzolv.shareware.data.entities.SplitType splitType) {
        // For now only equal strategy implemented; extend with a strategy registry
        if (splitType == com.fzolv.shareware.data.entities.SplitType.EQUAL) {
            return equalSplitStrategy;
        }
        throw new UnsupportedOperationException("Split type not supported: " + splitType);
    }
}
