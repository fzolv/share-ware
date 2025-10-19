package com.fzolv.shareware.hull.mapper;

import com.fzolv.shareware.hull.models.dtos.BalanceEntryDto;

import java.util.List;

public interface BalanceMapper {
    BalanceEntryDto toDto(String fromUserId, String toUserId, Double amount);

    List<BalanceEntryDto> toDtoList(List<BalanceEntryDto> entries);
}
