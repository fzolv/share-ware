package com.fzolv.shareware.hull.services.impl;

import com.fzolv.shareware.core.exceptions.ResourceAlreadyExistsException;
import com.fzolv.shareware.core.models.dtos.UserDto;
import com.fzolv.shareware.data.entities.GroupEntity;
import com.fzolv.shareware.data.entities.GroupMemberEntity;
import com.fzolv.shareware.data.entities.GroupRole;
import com.fzolv.shareware.data.entities.UserEntity;
import com.fzolv.shareware.data.repositories.GroupMemberRepository;
import com.fzolv.shareware.data.repositories.GroupRepository;
import com.fzolv.shareware.hull.events.EventPublisher;
import com.fzolv.shareware.hull.mapper.GroupMapper;
import com.fzolv.shareware.hull.models.dtos.GroupDto;
import com.fzolv.shareware.hull.models.dtos.GroupMemberDto;
import com.fzolv.shareware.hull.models.requests.GroupMemberRequest;
import com.fzolv.shareware.hull.models.requests.GroupRequest;
import com.fzolv.shareware.hull.services.GroupService;
import com.fzolv.shareware.hull.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final UserService userService;
    private final EventPublisher eventPublisher;

    @Value("${user-service.url}")
    private String userServiceUrl;
    private final GroupMapper mapper;

    @Override
    @Transactional
    public GroupDto createGroup(GroupRequest request, String createdById) {

        UserDto creatorDto = userService.getUserById(createdById);
        if (creatorDto == null) throw new EntityNotFoundException("Creator not found");

        UserEntity creator = new UserEntity();
        creator.setId(UUID.fromString(createdById));
        creator.setName(creatorDto.getName());
        creator.setEmail(creatorDto.getEmail());
        creator.setPhone(creatorDto.getPhone());
        creator.setCreatedAt(creatorDto.getCreatedAt());

        GroupEntity group = mapper.toEntity(request, creator);
        GroupEntity savedGroup = groupRepository.save(group);

        // Add creator as admin
        GroupMemberEntity creatorMember = mapper.toMemberEntity(
                new GroupMemberRequest() {{
                    setUserId(creator.getId().toString());
                    setRole(GroupRole.GROUP_ADMIN.name());
                }},
                savedGroup,
                creator
        );
        memberRepository.save(creatorMember);

        // Add other members if specified
        if (request.getMemberIds() != null) {
            for (String memberId : request.getMemberIds()) {
                if (!memberId.equals(createdById)) {
                    UserDto memberDto = userService.getUserById(memberId);
                    if (memberDto == null) throw new EntityNotFoundException("User not found: " + memberId);
                    UserEntity member = new UserEntity();
                    member.setId(UUID.fromString(memberDto.getId()));
                    member.setName(memberDto.getName());
                    member.setEmail(memberDto.getEmail());
                    member.setPhone(memberDto.getPhone());
                    member.setCreatedAt(memberDto.getCreatedAt());
                    GroupMemberEntity groupMember = mapper.toMemberEntity(
                            new GroupMemberRequest() {{
                                setUserId(member.getId().toString());
                                setRole(GroupRole.GROUP_MEMBER.name());
                            }},
                            savedGroup,
                            member
                    );
                    memberRepository.save(groupMember);
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("groupId", savedGroup.getId().toString());
                    payload.put("invitedUserId", member.getId().toString());
                    Set<String> userIds = new HashSet<>();
                    userIds.add(member.getId().toString());
                    userIds.add(creator.getId().toString());
                    payload.put("userIds", userIds);
                    eventPublisher.publish("shareware.group.events", "GROUP_MEMBER_INVITED", payload);
                }
            }
        }

        return mapper.toDto(savedGroup);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupDto getGroupById(String groupId) {
        return groupRepository.findById(UUID.fromString(groupId))
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));
    }

    @Override
    @Transactional
    public GroupDto updateGroup(String groupId, GroupRequest request) {
        GroupEntity group = groupRepository.findById(UUID.fromString(groupId))
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

        group.setName(request.getName());
        group.setDescription(request.getDescription());

        return mapper.toDto(groupRepository.save(group));
    }

    @Override
    @Transactional
    public void deleteGroup(String groupId) {
        if (!groupRepository.existsById(UUID.fromString(groupId))) {
            throw new EntityNotFoundException("Group not found");
        }
        groupRepository.deleteById(UUID.fromString(groupId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupDto> getAllGroups() {
        return groupRepository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupDto> getGroupsByUserId(String userId) {
        return memberRepository.findByUserId(UUID.fromString(userId)).stream()
                .map(GroupMemberEntity::getGroup)
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GroupMemberDto addMember(String groupId, GroupMemberRequest request) {
        GroupEntity group = groupRepository.findById(UUID.fromString(groupId))
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));


        UserDto userDto = userService.getUserById(request.getUserId());
        if (userDto == null) throw new EntityNotFoundException("User not found");
        UserEntity user = new UserEntity();
        user.setId(UUID.fromString(userDto.getId()));
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPhone(userDto.getPhone());
        user.setCreatedAt(userDto.getCreatedAt());

        // Check if already a member
        if (memberRepository.findByGroupIdAndUserId(
                UUID.fromString(groupId),
                UUID.fromString(request.getUserId())).isPresent()) {
            throw new ResourceAlreadyExistsException("User is already a member of this group");
        }

        GroupMemberEntity member = new GroupMemberEntity();
        member.setGroup(group);
        member.setUser(user);
        member.setRole(GroupRole.valueOf(request.getRole()));
        member.setJoinedAt(LocalDateTime.now());

        return mapper.toMemberDto(memberRepository.save(member));
    }

    @Override
    @Transactional
    public void removeMember(String groupId, String userId) {
        memberRepository.findByGroupIdAndUserId(
                        UUID.fromString(groupId),
                        UUID.fromString(userId))
                .ifPresent(memberRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupMemberDto> getGroupMembers(String groupId) {
        return memberRepository.findByGroupId(UUID.fromString(groupId)).stream()
                .map(mapper::toMemberDto)
                .collect(Collectors.toList());
    }
}