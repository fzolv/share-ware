package com.fzolv.shareware.data.repositories;

import com.fzolv.shareware.data.entities.GroupMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, UUID> {
    List<GroupMemberEntity> findByUserId(UUID userId);
    List<GroupMemberEntity> findByGroupId(UUID groupId);
    Optional<GroupMemberEntity> findByGroupIdAndUserId(UUID groupId, UUID userId);
}
