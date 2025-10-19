package com.fzolv.shareware.hull.models.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GroupMemberRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    private String role = "MEMBER"; // Default role
}