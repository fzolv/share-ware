package com.fzolv.shareware.hull.models.dtos;

import lombok.Data;

@Data
public class ExpenseSplitDto {
    private String id;
    private String userId;
    private Double amountOwed;
    private Double percentage;
}
