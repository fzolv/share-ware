package com.fzolv.shareware.user.services.impl;

import com.fzolv.shareware.data.entities.UserEntity;
import com.fzolv.shareware.data.repositories.UserRepository;
import com.fzolv.shareware.user.mapper.UserMapper;
import com.fzolv.shareware.core.models.dtos.UserDto;
import com.fzolv.shareware.core.models.requests.UserRequest;
import com.fzolv.shareware.user.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;

    @Override
    @Transactional
    public UserDto createUser(UserRequest request) {
        UserEntity user = mapper.toEntity(request);
        user.setCreatedAt(LocalDateTime.now());
        return mapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(String userId) {
    return userRepository.findById(UUID.fromString(userId))
        .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    @Override
    @Transactional
    public UserDto updateUser(String userId, UserRequest request) {
        UserEntity user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        mapper.updateEntityFromRequest(request, user);
        return mapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        if (!userRepository.existsById(UUID.fromString(userId))) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(UUID.fromString(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
    return userRepository.findAll().stream()
        .map(mapper::toDto)
        .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
    return userRepository.findByEmail(email)
        .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

}
