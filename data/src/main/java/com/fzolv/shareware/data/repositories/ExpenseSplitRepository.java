package com.fzolv.shareware.data.repositories;

import com.fzolv.shareware.data.entities.ExpenseSplitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplitEntity, UUID> {

    List<ExpenseSplitEntity> findByExpense_Group_Id(UUID groupId);

    List<ExpenseSplitEntity> findByUser_Id(UUID userId);

    List<ExpenseSplitEntity> findByExpenseId(UUID expenseId);

    ExpenseSplitEntity findByExpenseIdAndUserId(UUID expenseId, UUID userId);
}
