package com.fzolv.shareware.hull.models.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupMemberDto {
    private String id;
    private String userId;
    private String userName;
    private String role;
    private LocalDateTime joinedAt;
}