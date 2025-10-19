package com.fzolv.shareware.expense.models.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
public class ExpenseDto {
    private String id;
    private String groupId;
    private String description;
    private Double amount;
    private String paidById;
    private String currency;
    private String splitType;
    private LocalDateTime createdAt;
    private List<ExpenseSplitDto> splits;
}
