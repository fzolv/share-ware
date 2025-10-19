package com.fzolv.shareware.user.mapper;

import com.fzolv.shareware.data.entities.UserEntity;
import com.fzolv.shareware.core.models.dtos.UserDto;
import com.fzolv.shareware.core.models.requests.UserRequest;

public interface UserMapper {
    UserDto toDto(UserEntity entity);
    UserEntity toEntity(UserRequest request);
    void updateEntityFromRequest(UserRequest request, UserEntity entity);
}
