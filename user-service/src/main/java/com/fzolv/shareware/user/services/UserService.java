package com.fzolv.shareware.user.services;

import com.fzolv.shareware.core.models.dtos.UserDto;
import com.fzolv.shareware.core.models.requests.UserRequest;
import java.util.List;

public interface UserService {
    UserDto createUser(UserRequest request);
    UserDto getUserById(String userId);
    UserDto updateUser(String userId, UserRequest request);
    void deleteUser(String userId);
    List<UserDto> getAllUsers();
    UserDto getUserByEmail(String email);
}
