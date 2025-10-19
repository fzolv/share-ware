package com.fzolv.shareware.core.models.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {
    private String id;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
}
