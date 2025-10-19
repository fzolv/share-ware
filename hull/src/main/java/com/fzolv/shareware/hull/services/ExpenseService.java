package com.fzolv.shareware.hull.services;

import com.fzolv.shareware.hull.models.dtos.ExpenseDto;
import com.fzolv.shareware.hull.models.requests.ExpenseRequest;

import java.util.List;

public interface ExpenseService {
    ExpenseDto createExpense(ExpenseRequest request);

    ExpenseDto updateExpense(String expenseId, ExpenseRequest request);

    void deleteExpense(String expenseId);

    ExpenseDto getExpenseById(String expenseId);

    List<ExpenseDto> getExpensesByGroupId(String groupId);
}
