package com.fzolv.shareware.hull.services;

import com.fzolv.shareware.hull.models.dtos.GroupDto;
import com.fzolv.shareware.hull.models.dtos.GroupMemberDto;
import com.fzolv.shareware.hull.models.requests.GroupMemberRequest;
import com.fzolv.shareware.hull.models.requests.GroupRequest;

import java.util.List;

public interface GroupService {
    GroupDto createGroup(GroupRequest request, String createdById);

    GroupDto getGroupById(String groupId);

    GroupDto updateGroup(String groupId, GroupRequest request);

    void deleteGroup(String groupId);

    List<GroupDto> getAllGroups();

    List<GroupDto> getGroupsByUserId(String userId);

    // Member management
    GroupMemberDto addMember(String groupId, GroupMemberRequest request);

    void removeMember(String groupId, String userId);

    List<GroupMemberDto> getGroupMembers(String groupId);
}