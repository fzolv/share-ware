package com.fzolv.shareware.balance.mapper;

import com.fzolv.shareware.balance.models.dtos.BalanceEntryDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BalanceMapperImpl implements BalanceMapper {

    @Override
    public BalanceEntryDto toDto(String fromUserId, String toUserId, Double amount) {
        BalanceEntryDto dto = new BalanceEntryDto();
        dto.setFromUserId(fromUserId);
        dto.setToUserId(toUserId);
        dto.setAmount(amount);
        return dto;
    }

    @Override
    public List<BalanceEntryDto> toDtoList(List<BalanceEntryDto> entries) {
        return entries.stream().collect(Collectors.toList());
    }
}
