package com.fzolv.shareware.hull.controllers;

import com.fzolv.shareware.hull.models.dtos.GroupDto;
import com.fzolv.shareware.hull.models.dtos.GroupMemberDto;
import com.fzolv.shareware.hull.models.requests.GroupMemberRequest;
import com.fzolv.shareware.hull.models.requests.GroupRequest;
import com.fzolv.shareware.hull.services.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).USER, null, 'MEMBER','ADMIN')")
    public ResponseEntity<GroupDto> createGroup(
            @Valid @RequestBody GroupRequest request, Authentication authentication) {
        return new ResponseEntity<>(groupService.createGroup(request, authentication.getPrincipal().toString()), HttpStatus.CREATED);
    }

    @GetMapping("/{groupId}")
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).GROUP, #groupId, 'GROUP_MEMBER','GROUP_ADMIN','ADMIN')")
    public ResponseEntity<GroupDto> getGroup(@P("groupId") @PathVariable("groupId") String groupId) {
        return ResponseEntity.ok(groupService.getGroupById(groupId));
    }

    @PutMapping("/{groupId}")
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).GROUP, #groupId, 'GROUP_ADMIN','ADMIN')")
    public ResponseEntity<GroupDto> updateGroup(
            @P("groupId") @PathVariable("groupId") String groupId,
            @Valid @RequestBody GroupRequest request) {
        return ResponseEntity.ok(groupService.updateGroup(groupId, request));
    }

    @DeleteMapping("/{groupId}")
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).GROUP, #groupId, 'GROUP_ADMIN','ADMIN')")
    public ResponseEntity<Void> deleteGroup(@P("groupId") @PathVariable("groupId") String groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).USER, null, 'MEMBER','ADMIN')")
    public ResponseEntity<List<GroupDto>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).USER, #userId, 'MEMBER','ADMIN')")
    public ResponseEntity<List<GroupDto>> getGroupsByUser(@P("userId") @PathVariable("userId") String userId) {
        return ResponseEntity.ok(groupService.getGroupsByUserId(userId));
    }

    // Group member management endpoints
    @PostMapping("/{groupId}/members")
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).GROUP, #groupId, 'GROUP_ADMIN','ADMIN')")
    public ResponseEntity<GroupMemberDto> addMember(
            @P("groupId") @PathVariable("groupId") String groupId,
            @Valid @RequestBody GroupMemberRequest request) {
        return new ResponseEntity<>(groupService.addMember(groupId, request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).GROUP, #groupId, 'GROUP_ADMIN','ADMIN')")
    public ResponseEntity<Void> removeMember(
            @P("groupId") @PathVariable("groupId") String groupId,
            @PathVariable String userId) {
        groupService.removeMember(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}/members")
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).GROUP, #groupId, 'GROUP_MEMBER','GROUP_ADMIN','ADMIN')")
    public ResponseEntity<List<GroupMemberDto>> getGroupMembers(@P("groupId") @PathVariable("groupId") String groupId) {
        return ResponseEntity.ok(groupService.getGroupMembers(groupId));
    }
}