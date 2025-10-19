package com.fzolv.shareware.expense.mapper;

import com.fzolv.shareware.data.entities.ExpenseEntity;
import com.fzolv.shareware.data.entities.ExpenseSplitEntity;
import com.fzolv.shareware.expense.models.dtos.ExpenseDto;
import com.fzolv.shareware.expense.models.dtos.ExpenseSplitDto;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ExpenseMapperImpl implements ExpenseMapper {

    @Override
    public ExpenseDto toDto(ExpenseEntity entity) {
        if (entity == null) return null;
        ExpenseDto dto = new ExpenseDto();
        dto.setId(entity.getId().toString());
        dto.setGroupId(entity.getGroup() != null ? entity.getGroup().getId().toString() : null);
        dto.setDescription(entity.getDescription());
        dto.setAmount(entity.getAmount());
        dto.setPaidById(entity.getPaidBy() != null ? entity.getPaidBy().getId().toString() : null);
        dto.setCurrency(entity.getCurrency());
        dto.setSplitType(entity.getSplitType() != null ? entity.getSplitType().name() : null);
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setSplits(entity.getSplits().stream().map(this::toSplitDto).collect(Collectors.toList()));
        return dto;
    }

    @Override
    public ExpenseSplitDto toSplitDto(ExpenseSplitEntity entity) {
        if (entity == null) return null;
        ExpenseSplitDto dto = new ExpenseSplitDto();
        dto.setId(entity.getId().toString());
        dto.setUserId(entity.getUser() != null ? entity.getUser().getId().toString() : null);
        dto.setAmountOwed(entity.getAmountOwed());
        dto.setPercentage(entity.getPercentage());
        return dto;
    }
}
