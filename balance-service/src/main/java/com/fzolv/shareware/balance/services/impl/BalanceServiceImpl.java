package com.fzolv.shareware.balance.services.impl;

import com.fzolv.shareware.balance.mapper.BalanceMapper;
import com.fzolv.shareware.balance.models.dtos.BalanceEntryDto;
import com.fzolv.shareware.balance.services.BalanceService;
import com.fzolv.shareware.data.entities.ExpenseSplitEntity;
import com.fzolv.shareware.data.repositories.ExpenseRepository;
import com.fzolv.shareware.data.repositories.ExpenseSplitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    private final ExpenseSplitRepository splitRepository;
    private final ExpenseRepository expenseRepository;
    private final BalanceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<BalanceEntryDto> getGroupBalanceSheet(String groupId) {
        UUID gid = UUID.fromString(groupId);
        List<ExpenseSplitEntity> splits = splitRepository.findByExpense_Group_Id(gid);
        if (splits.isEmpty()) return Collections.emptyList();

        // net position per user = paid - owed
        Map<UUID, Double> net = new HashMap<>();

        // A simpler and correct approach: for each expense, add amount to payer, subtract per-split from participants
        List<java.util.UUID> expenses = splits.stream().map(s -> s.getExpense().getId()).distinct().collect(Collectors.toList());
        for (UUID expenseId : expenses) {
            var expOpt = expenseRepository.findById(expenseId);
            if (expOpt.isEmpty()) continue;
            var exp = expOpt.get();
            UUID payer = exp.getPaidBy().getId();
            net.putIfAbsent(payer, 0.0);
            net.put(payer, net.get(payer) + exp.getAmount());

            List<ExpenseSplitEntity> expenseSplits = splits.stream().filter(s -> s.getExpense().getId().equals(expenseId)).collect(Collectors.toList());
            for (ExpenseSplitEntity s : expenseSplits) {
                UUID u = s.getUser().getId();
                net.putIfAbsent(u, 0.0);
                net.put(u, net.get(u) - s.getAmountOwed());
            }
        }

        // Now net holds net positions; positive => should receive, negative => owes
        List<Map.Entry<UUID, Double>> receivers = net.entrySet().stream().filter(e -> e.getValue() > 0).sorted(Map.Entry.<UUID, Double>comparingByValue().reversed()).collect(Collectors.toList());
        List<Map.Entry<UUID, Double>> payers = net.entrySet().stream().filter(e -> e.getValue() < 0).sorted(Map.Entry.comparingByValue()).collect(Collectors.toList());

        List<BalanceEntryDto> result = new ArrayList<>();

        int i = 0, j = 0;
        while (i < payers.size() && j < receivers.size()) {
            var payer = payers.get(i);
            var receiver = receivers.get(j);
            double amount = Math.min(-payer.getValue(), receiver.getValue());
            if (amount <= 0) break;
            result.add(mapper.toDto(payer.getKey().toString(), receiver.getKey().toString(), Math.round(amount * 100.0) / 100.0));
            payer.setValue(payer.getValue() + amount);
            receiver.setValue(receiver.getValue() - amount);
            if (Math.abs(payer.getValue()) < 0.01) i++;
            if (Math.abs(receiver.getValue()) < 0.01) j++;
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BalanceEntryDto> getUserBalanceSheet(String userId) {
        UUID uid = UUID.fromString(userId);
        // compute group-level balances and filter entries that involve the user
        // For simplicity, compute over all groups the user is part of by looking at splits
        List<ExpenseSplitEntity> splits = splitRepository.findByUser_Id(uid);
        if (splits.isEmpty()) return Collections.emptyList();

        // Reuse group computation by grouping splits by group
        Map<UUID, List<ExpenseSplitEntity>> byGroup = splits.stream().collect(Collectors.groupingBy(s -> s.getExpense().getGroup().getId()));
        List<BalanceEntryDto> result = new ArrayList<>();
        for (var entry : byGroup.entrySet()) {
            List<ExpenseSplitEntity> groupSplits = splitRepository.findByExpense_Group_Id(entry.getKey());
            // compute group result
            List<BalanceEntryDto> groupBalances = getGroupBalanceSheet(entry.getKey().toString());
            // filter for user
            for (BalanceEntryDto be : groupBalances) {
                if (be.getFromUserId().equals(userId) || be.getToUserId().equals(userId)) {
                    result.add(be);
                }
            }
        }
        return result;
    }
}
