package com.fzolv.shareware.expense.services.impl;

import com.fzolv.shareware.data.entities.*;
import com.fzolv.shareware.data.repositories.ExpenseRepository;
import com.fzolv.shareware.data.repositories.GroupRepository;
import com.fzolv.shareware.data.repositories.UserRepository;
import com.fzolv.shareware.expense.mapper.ExpenseMapper;
import com.fzolv.shareware.expense.models.dtos.ExpenseDto;
import com.fzolv.shareware.expense.models.requests.ExpenseRequest;
import com.fzolv.shareware.expense.services.ExpenseService;
import com.fzolv.shareware.expense.strategy.ExpenseSplitStrategy;
import com.fzolv.shareware.expense.strategy.impl.EqualSplitStrategy;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
        expense.setCreatedAt(LocalDateTime.now());

        expense = expenseRepository.save(expense);

        // calculate splits using strategy
        ExpenseSplitStrategy strategy = resolveStrategy(expense.getSplitType());
        List<ExpenseSplitEntity> splits = strategy.calculateSplits(expense, request.getSplits(), userRepository);
        expense.setSplits(splits);
        expense = expenseRepository.save(expense);

        return mapper.toDto(expense);
    }

    @Override
    @Transactional
    public ExpenseDto updateExpense(String expenseId, ExpenseRequest request) {
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

        expense = expenseRepository.save(expense);
        return mapper.toDto(expense);
    }

    @Override
    @Transactional
    public void deleteExpense(String expenseId) {
        if (!expenseRepository.existsById(UUID.fromString(expenseId))) {
            throw new EntityNotFoundException("Expense not found");
        }
        expenseRepository.deleteById(UUID.fromString(expenseId));
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
