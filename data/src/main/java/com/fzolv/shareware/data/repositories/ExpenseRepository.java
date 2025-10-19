package com.fzolv.shareware.data.repositories;

import com.fzolv.shareware.data.entities.ExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, UUID> {
	java.util.List<ExpenseEntity> findByGroupId(java.util.UUID groupId);
}
