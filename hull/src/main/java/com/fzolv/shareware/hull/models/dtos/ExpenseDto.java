package com.fzolv.shareware.hull.models.dtos;

import lombok.Data;

import java.time.LocalDateTime;
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
