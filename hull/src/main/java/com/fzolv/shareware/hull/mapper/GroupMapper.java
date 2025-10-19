package com.fzolv.shareware.hull.mapper;

import com.fzolv.shareware.data.entities.GroupEntity;
import com.fzolv.shareware.data.entities.GroupMemberEntity;
import com.fzolv.shareware.hull.models.dtos.GroupDto;
import com.fzolv.shareware.hull.models.dtos.GroupMemberDto;
import com.fzolv.shareware.hull.models.requests.GroupMemberRequest;
import com.fzolv.shareware.hull.models.requests.GroupRequest;

public interface GroupMapper {
    GroupDto toDto(GroupEntity entity);

    GroupMemberDto toMemberDto(GroupMemberEntity entity);

    GroupEntity toEntity(GroupRequest request, com.fzolv.shareware.data.entities.UserEntity creator);

    GroupMemberEntity toMemberEntity(GroupMemberRequest request, GroupEntity group, com.fzolv.shareware.data.entities.UserEntity user);
}
