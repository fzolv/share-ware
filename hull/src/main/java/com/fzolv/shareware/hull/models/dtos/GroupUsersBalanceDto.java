package com.fzolv.shareware.hull.models.dtos;

import lombok.Data;

@Data
public class GroupUsersBalanceDto {
    private String id;
    private String balanceId;
    private String lender;
    private String borrower;
    private Double amount;
}


