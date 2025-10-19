package com.fzolv.shareware.data.repositories;

import com.fzolv.shareware.data.entities.GroupBalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupBalanceRepository extends JpaRepository<GroupBalanceEntity, UUID> {
    Optional<GroupBalanceEntity> findByGroup_Id(UUID groupId);
}


