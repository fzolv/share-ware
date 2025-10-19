package com.fzolv.shareware.hull.services.impl;

import com.fzolv.shareware.data.entities.BalanceEntity;
import com.fzolv.shareware.data.entities.ExpenseEntity;
import com.fzolv.shareware.data.entities.ExpenseSplitEntity;
import com.fzolv.shareware.data.entities.UserEntity;
import com.fzolv.shareware.data.repositories.BalanceRepository;
import com.fzolv.shareware.data.repositories.ExpenseRepository;
import com.fzolv.shareware.data.repositories.ExpenseSplitRepository;
import com.fzolv.shareware.data.repositories.UserRepository;
import com.fzolv.shareware.data.repositories.SettlementRepository;
import com.fzolv.shareware.data.entities.SettlementEntity;
import com.fzolv.shareware.hull.locks.LockManager;
import com.fzolv.shareware.hull.models.requests.SettlementRequest;
import com.fzolv.shareware.hull.services.SettlementService;
import com.fzolv.shareware.hull.events.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository splitRepository;
    private final BalanceRepository balanceRepository;
    private final UserRepository userRepository;
    private final LockManager lockManager;
    private final EventPublisher eventPublisher;
    private final SettlementRepository settlementRepository;

    @Override
    @Transactional
    public void settleExpense(String expenseId, SettlementRequest request) {
        String lockKey = "expense:" + expenseId;
        lockManager.lock(lockKey);
        try {
            UUID expId = UUID.fromString(expenseId);
            Optional<ExpenseEntity> expOpt = expenseRepository.findById(expId);
            if (expOpt.isEmpty()) throw new IllegalArgumentException("Expense not found: " + expenseId);
            ExpenseEntity expense = expOpt.get();

            UUID fromUid = UUID.fromString(request.getFromUserId());
            UUID toUid = UUID.fromString(request.getToUserId());

            // ensure users exist
            UserEntity fromUser = userRepository.findById(fromUid).orElseThrow(() -> new IllegalArgumentException("From user not found"));
            UserEntity toUser = userRepository.findById(toUid).orElseThrow(() -> new IllegalArgumentException("To user not found"));

        /*
         * Payment gateway integration stub:
         * - This is the point to initiate a real payment (e.g., Stripe, Razorpay, PayPal).
         * - Example flow (sync):
         *   paymentGateway.charge(fromUid, toUid, request.getAmount(), expense.getCurrency());
         * - Example flow (async):
         *   1) Create a payment intent/session and return client secrets to caller
         *   2) On webhook/callback, verify signature and then call this settle flow
         * - Make sure to implement idempotency (e.g., using a payment reference key) to avoid double charges.
         */

            // decrease split owed for fromUser for this expense
            Optional<ExpenseSplitEntity> splitOpt = Optional.ofNullable(splitRepository.findByExpenseIdAndUserId(expId, fromUid));
            if (splitOpt.isEmpty()) throw new IllegalArgumentException("Split not found for user on expense");
            ExpenseSplitEntity split = splitOpt.get();
            double owed = split.getAmountOwed();
            double settleAmount = Math.min(owed, request.getAmount());
            split.setAmountOwed(owed - settleAmount);
            splitRepository.save(split);

            // update balance entity: reduce amount owed from fromUser to toUser
            // balance unique constraint (group, from_user, to_user)
            UUID groupId = expense.getGroup().getId();
            Optional<BalanceEntity> balanceOpt = balanceRepository.findAll().stream()
                    .filter(b -> b.getGroup().getId().equals(groupId)
                            && b.getFromUser().getId().equals(fromUid)
                            && b.getToUser().getId().equals(toUid))
                    .findFirst();
            BalanceEntity balance;
            if (balanceOpt.isPresent()) {
                balance = balanceOpt.get();
                balance.setAmount(balance.getAmount() - settleAmount);
            } else {
                balance = new BalanceEntity();
                balance.setGroup(expense.getGroup());
                balance.setFromUser(fromUser);
                balance.setToUser(toUser);
                balance.setAmount(-settleAmount);
            }
            balanceRepository.save(balance);

            // optionally mark expense as settled when all splits are zero
            boolean allZero = splitRepository.findByExpenseId(expId).stream().allMatch(s -> s.getAmountOwed() <= 0.001);
            if (allZero) {
                expense.setAmount(0.0);
                expenseRepository.save(expense);
            }

            // Persist settlement record
            SettlementEntity settlement = new SettlementEntity();
            settlement.setExpenseId(expense.getId());
            settlement.setFromUserId(fromUid);
            settlement.setToUserId(toUid);
            settlement.setAmount(settleAmount);
            settlementRepository.save(settlement);

            /*
             * Payment gateway integration stub (post-settlement):
             * - If We gateway returns a transactionId/chargeId, persist it here (extend SettlementEntity accordingly).
             * - We can also emit an event to a notifications/payments topic for reconciliation:
             *   eventPublisher.publish("shareware.payments.events", "PAYMENT_CAPTURED", Map.of(
             *       "expenseId", expenseId,
             *       "fromUserId", request.getFromUserId(),
             *       "toUserId", request.getToUserId(),
             *       "amount", settleAmount
             *   ));
             */

            Map<String, Object> payload = new HashMap<>();
            payload.put("expenseId", expenseId);
            payload.put("fromUserId", request.getFromUserId());
            payload.put("toUserId", request.getToUserId());
            payload.put("amount", request.getAmount());
            Set<String> userIds = new HashSet<>();
            userIds.add(request.getFromUserId());
            userIds.add(request.getToUserId());
            payload.put("userIds", userIds);
            eventPublisher.publish("shareware.settlement.events", "EXPENSE_SETTLED", payload);
        } finally {
            lockManager.releaseLock(lockKey);
        }

    }
}
