package com.fzolv.shareware.hull.services;

import com.fzolv.shareware.hull.models.dtos.BalanceEntryDto;

import java.util.List;

public interface BalanceService {
    List<BalanceEntryDto> getGroupBalanceSheet(String groupId);

    List<BalanceEntryDto> getUserBalanceSheet(String userId);
}
