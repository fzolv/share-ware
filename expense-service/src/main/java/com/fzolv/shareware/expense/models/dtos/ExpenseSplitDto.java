package com.fzolv.shareware.expense.models.dtos;

import lombok.Data;

@Data
public class ExpenseSplitDto {
    private String id;
    private String userId;
    private Double amountOwed;
    private Double percentage;
}
