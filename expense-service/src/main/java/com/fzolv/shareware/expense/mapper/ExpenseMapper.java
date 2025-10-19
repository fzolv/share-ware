package com.fzolv.shareware.expense.mapper;

import com.fzolv.shareware.data.entities.ExpenseEntity;
import com.fzolv.shareware.data.entities.ExpenseSplitEntity;
import com.fzolv.shareware.expense.models.dtos.ExpenseDto;
import com.fzolv.shareware.expense.models.dtos.ExpenseSplitDto;

public interface ExpenseMapper {
    ExpenseDto toDto(ExpenseEntity entity);
    ExpenseSplitDto toSplitDto(ExpenseSplitEntity entity);
}
