package com.fzolv.shareware.hull.models.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupDto {
    private String id;
    private String name;
    private String description;
    private String createdById;
    private LocalDateTime createdAt;
    private List<GroupMemberDto> members;
}