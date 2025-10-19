package com.fzolv.shareware.balance.models.dtos;

import lombok.Data;

@Data
public class BalanceEntryDto {
    private String fromUserId;
    private String toUserId;
    private Double amount;
}
