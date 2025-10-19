package com.fzolv.shareware.hull.mapper;

import com.fzolv.shareware.data.entities.GroupEntity;
import com.fzolv.shareware.data.entities.GroupMemberEntity;
import com.fzolv.shareware.data.entities.GroupRole;
import com.fzolv.shareware.data.entities.UserEntity;
import com.fzolv.shareware.hull.models.dtos.GroupDto;
import com.fzolv.shareware.hull.models.dtos.GroupMemberDto;
import com.fzolv.shareware.hull.models.requests.GroupMemberRequest;
import com.fzolv.shareware.hull.models.requests.GroupRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Component
public class GroupMapperImpl implements GroupMapper {

    @Override
    public GroupDto toDto(GroupEntity entity) {
        if (entity == null) return null;
        GroupDto dto = new GroupDto();
        dto.setId(entity.getId().toString());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCreatedById(entity.getCreatedBy() != null ? entity.getCreatedBy().getId().toString() : null);
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setMembers(entity.getMembers().stream().map(this::toMemberDto).collect(Collectors.toList()));
        return dto;
    }

    @Override
    public GroupMemberDto toMemberDto(GroupMemberEntity entity) {
        if (entity == null) return null;
        GroupMemberDto dto = new GroupMemberDto();
        dto.setId(entity.getId().toString());
        dto.setUserId(entity.getUser() != null ? entity.getUser().getId().toString() : null);
        dto.setUserName(entity.getUser() != null ? entity.getUser().getName() : null);
        dto.setRole(entity.getRole() != null ? entity.getRole().name() : null);
        dto.setJoinedAt(entity.getJoinedAt());
        return dto;
    }

    @Override
    public GroupEntity toEntity(GroupRequest request, UserEntity creator) {
        if (request == null) return null;
        GroupEntity entity = new GroupEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setCreatedBy(creator);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    @Override
    public GroupMemberEntity toMemberEntity(GroupMemberRequest request, GroupEntity group, UserEntity user) {
        if (request == null) return null;
        GroupMemberEntity member = new GroupMemberEntity();
        member.setGroup(group);
        member.setUser(user);
        try {
            member.setRole(GroupRole.valueOf(request.getRole()));
        } catch (Exception ex) {
            member.setRole(GroupRole.GROUP_MEMBER);
        }
        member.setJoinedAt(LocalDateTime.now());
        return member;
    }
}
