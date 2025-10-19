package com.fzolv.shareware.data.repositories;

import com.fzolv.shareware.data.entities.SettlementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SettlementRepository extends JpaRepository<SettlementEntity, UUID> {
    List<SettlementEntity> findByExpenseIdAndCreatedAtGreaterThan(UUID expenseId, LocalDateTime lastCheck);


}
