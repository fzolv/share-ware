package com.fzolv.shareware.core.exceptions;

public class ExpenseNotFoundException extends ApiException {
    public ExpenseNotFoundException(String expenseId) {
        super("EXPENSE_NOT_FOUND", 404, "Expense not found: " + expenseId);
    }
}
