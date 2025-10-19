package com.fzolv.shareware.hull.models.dtos;

import lombok.Data;

@Data
public class BalanceEntryDto {
    private String fromUserId;
    private String toUserId;
    private Double amount;
}
