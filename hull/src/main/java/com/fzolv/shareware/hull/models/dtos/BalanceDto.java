package com.fzolv.shareware.hull.models.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BalanceDto {
    private String balanceId;
    private String groupId;
    private LocalDateTime calcAt;
}


