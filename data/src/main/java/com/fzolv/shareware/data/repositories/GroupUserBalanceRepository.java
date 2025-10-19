package com.fzolv.shareware.data.repositories;

import com.fzolv.shareware.data.entities.GroupUserBalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupUserBalanceRepository extends JpaRepository<GroupUserBalanceEntity, UUID> {
    List<GroupUserBalanceEntity> findByBalance_Id(UUID groupId);

    List<GroupUserBalanceEntity> findByBalance_Group_Id(UUID groupId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from GroupUserBalanceEntity g where g.balance.id = :balanceId")
    int deleteByBalanceId(@Param("balanceId") UUID balanceId);
}


