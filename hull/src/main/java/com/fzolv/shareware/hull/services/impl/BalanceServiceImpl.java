package com.fzolv.shareware.hull.services.impl;

import com.fzolv.shareware.data.entities.*;
import com.fzolv.shareware.data.repositories.*;
import com.fzolv.shareware.hull.locks.LockManager;
import com.fzolv.shareware.hull.models.dtos.BalanceDto;
import com.fzolv.shareware.hull.models.dtos.BalanceEntryDto;
import com.fzolv.shareware.hull.services.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    private final ExpenseSplitRepository splitRepository;
    private final ExpenseRepository expenseRepository;
    private final BalanceRepository balanceRepository;
    private final SettlementRepository settlementRepository;
    private final LockManager lockManager;
    private final GroupBalanceRepository groupBalanceRepository;
    private final GroupUserBalanceRepository groupUserBalanceRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Override
    @Transactional
    public List<com.fzolv.shareware.hull.models.dtos.BalanceEntryDto> getGroupBalanceSheet(String groupId) {
        recalculateGroupBalances(groupId);
        UUID gid = UUID.fromString(groupId);
        return groupUserBalanceRepository.findByBalance_Group_Id(gid).stream()
                .map(row -> {
                    BalanceEntryDto dto = new BalanceEntryDto();
                    dto.setFromUserId(row.getBorrowerId().toString());
                    dto.setToUserId(row.getLenderId().toString());
                    dto.setAmount(row.getAmount());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<com.fzolv.shareware.hull.models.dtos.BalanceEntryDto> getUserBalanceSheet(String userId) {
        UUID uid = UUID.fromString(userId);
        // Recalculate balances for all groups this user belongs to
        List<UUID> groupIds = groupMemberRepository.findByUserId(uid).stream()
                .map(m -> m.getGroup().getId())
                .distinct()
                .toList();
        groupIds.forEach(gid -> recalculateGroupBalances(gid.toString()));

        List<BalanceEntryDto> result = new ArrayList<>();
        for (UUID gid : groupIds) {
            groupBalanceRepository.findByGroup_Id(gid).ifPresent(gb -> {
                for (GroupUserBalanceEntity row : groupUserBalanceRepository.findByBalance_Id(gb.getId())) {
                    if (uid.equals(row.getBorrowerId()) || uid.equals(row.getLenderId())) {
                        BalanceEntryDto dto = new BalanceEntryDto();
                        dto.setFromUserId(row.getBorrowerId().toString());
                        dto.setToUserId(row.getLenderId().toString());
                        dto.setAmount(row.getAmount());
                        result.add(dto);
                    }
                }
            });
        }
        return result;
    }

    // New algorithmic method to recalculate and persist balances with calcAt tracking
    @Transactional
    public List<BalanceDto> recalculateGroupBalances(String groupId) {
        UUID gid = UUID.fromString(groupId);
        String lockKey = "balance-group:" + groupId;
        lockManager.lock(lockKey);
        try {

            // 1) Check balance records for the group
            Optional<GroupBalanceEntity> existingOptional = groupBalanceRepository.findByGroup_Id(UUID.fromString(groupId));
            LocalDateTime lastCalcAt = existingOptional.map(GroupBalanceEntity::getCalcAt)
                    .orElse(LocalDateTime.of(1961, Month.JANUARY, 1, 0, 0));
            LocalDateTime targetCalcAt = LocalDateTime.now();

            // 2) Get delta: fetch expenses and splits since last calcAt
            // Collect expense ids for group updated since lastCalcAt
            List<UUID> expenseIds = splitRepository.findByExpense_Group_Id(gid).stream()
                    .map(s -> s.getExpense().getId())
                    .distinct()
                    .toList();

            // Fetch entities and filter by updatedAt (fallback to createdAt) on ExpenseEntity
            List<ExpenseEntity> expensesSince = expenseIds.stream()
                    .map(expenseRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(e -> {
                        java.time.LocalDateTime ts = e.getUpdatedAt() != null ? e.getUpdatedAt() : e.getCreatedAt();
                        return ts != null && !ts.isBefore(lastCalcAt) && ts.isBefore(targetCalcAt);
                    })
                    .toList();

            // 3) Initialize net from latest snapshot and apply delta to compute balances map lender<-borrower:amount
            Map<String, Double> net = new HashMap<>(); // key: borrowerId->lenderId
            groupBalanceRepository.findByGroup_Id(gid).ifPresent(gb -> {
                for (GroupUserBalanceEntity row : groupUserBalanceRepository.findByBalance_Id(gb.getId())) {
                    net.put(row.getBorrowerId().toString() + "->" + row.getLenderId().toString(), row.getAmount());
                }
            });
            for (ExpenseEntity exp : expensesSince) {
                UUID payer = exp.getPaidBy().getId();
                List<ExpenseSplitEntity> splits = splitRepository.findByExpenseId(exp.getId());
                for (ExpenseSplitEntity s : splits) {
                    UUID borrower = s.getUser().getId();
                    if (borrower.equals(payer)) continue;
                    String key = borrower + "->" + payer;
                    net.putIfAbsent(key, 0.0);
                    net.put(key, net.get(key) + s.getAmountOwed());
                }
            }

            // Apply settlement deltas within (lastCalcAt, targetCalcAt]
            for (UUID expId : expenseIds) {
                for (var st : settlementRepository.findByExpenseIdAndCreatedAtGreaterThan(expId, lastCalcAt)) {
                    if (st.getCreatedAt() == null) continue;
                    if (st.getCreatedAt().isAfter(lastCalcAt) && !st.getCreatedAt().isAfter(targetCalcAt)) {
                        String key = st.getFromUserId() + "->" + st.getToUserId();
                        net.putIfAbsent(key, 0.0);
                        net.put(key, Math.max(0.0, net.get(key) - st.getAmount()));
                    }
                }
            }

            // Normalize bilateral amounts: keep only one direction with the net difference
            Map<String, Double> normalized = new HashMap<>();
            Set<String> processedPairs = new HashSet<>();
            for (Map.Entry<String, Double> e : net.entrySet()) {
                String key = e.getKey();
                String[] parts = key.split("->");
                if (parts.length != 2) continue;
                String a = parts[0];
                String b = parts[1];
                String pairId = (a.compareTo(b) <= 0 ? a + "|" + b : b + "|" + a);
                if (processedPairs.contains(pairId)) continue;
                String reverseKey = b + "->" + a;
                double u = net.getOrDefault(key, 0.0);
                double v = net.getOrDefault(reverseKey, 0.0);
                if (u > v) {
                    double d = round2(u - v);
                    if (d > 0.0) normalized.put(a + "->" + b, d);
                } else if (v > u) {
                    double d = round2(v - u);
                    if (d > 0.0) normalized.put(b + "->" + a, d);
                }
                processedPairs.add(pairId);
            }

            GroupBalanceEntity groupBalance = groupBalanceRepository
                    .findByGroup_Id(gid)
                    .orElse(null);
            if (groupBalance == null) {
                groupBalance = new GroupBalanceEntity();
                GroupEntity g = new GroupEntity();
                g.setId(gid);
                groupBalance.setGroup(g);
            }
            groupBalance.setCalcAt(targetCalcAt);
            groupBalance = groupBalanceRepository.save(groupBalance);


            groupUserBalanceRepository.deleteByBalanceId(groupBalance.getId());

            List<GroupUserBalanceEntity> toSave = new ArrayList<>();
            for (Map.Entry<String, Double> e : normalized.entrySet()) {
                String[] parts = e.getKey().split("->");
                UUID borrowerId = UUID.fromString(parts[0]);
                UUID lenderId = UUID.fromString(parts[1]);
                double amount = round2(e.getValue());
                if (amount <= 0.0) continue;
                GroupUserBalanceEntity row = new GroupUserBalanceEntity();
                row.setBalance(groupBalance);
                row.setBorrowerId(borrowerId);
                row.setLenderId(lenderId);
                row.setAmount(amount);
                toSave.add(row);
            }
            if (!toSave.isEmpty()) {
                groupUserBalanceRepository.saveAll(toSave);
            }

            BalanceDto dto = new BalanceDto();
            dto.setBalanceId(groupBalance.getId().toString());
            dto.setGroupId(groupId);
            dto.setCalcAt(targetCalcAt);
            return java.util.List.of(dto);
        } finally {
            lockManager.releaseLock(lockKey);
        }
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
