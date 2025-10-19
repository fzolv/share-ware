package com.fzolv.shareware.balance.services;

import com.fzolv.shareware.balance.models.dtos.BalanceEntryDto;

import java.util.List;

public interface BalanceService {
    List<BalanceEntryDto> getGroupBalanceSheet(String groupId);
    List<BalanceEntryDto> getUserBalanceSheet(String userId);
}
