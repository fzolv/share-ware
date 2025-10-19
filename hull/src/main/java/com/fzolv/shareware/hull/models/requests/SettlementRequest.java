package com.fzolv.shareware.hull.models.requests;

import lombok.Data;

@Data
public class SettlementRequest {
    private String fromUserId; // who is paying
    private String toUserId;   // who receives (usually payer)
    private Double amount;
    private String currency;
}
