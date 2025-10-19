package com.fzolv.shareware.hull.controllers;

import com.fzolv.shareware.core.models.dtos.UserDto;
import com.fzolv.shareware.core.models.requests.UserRequest;
import com.fzolv.shareware.hull.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).USER, null, 'ADMIN')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserRequest request) {
        return new ResponseEntity<>(userService.createUser(request), HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).USER, #userId, 'MEMBER','ADMIN')")
    public ResponseEntity<UserDto> getUserById(@P("userId") @PathVariable("userId") String userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).USER, #userId, 'MEMBER','ADMIN')")
    public ResponseEntity<UserDto> updateUser(
            @P("userId") @PathVariable("userId") String userId,
            @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).USER, #userId, 'ADMIN')")
    public ResponseEntity<Void> deleteUser(@P("userId") @PathVariable("userId") String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).USER, null, 'ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("@resourceAuth.hasRole(T(com.fzolv.shareware.hull.security.ResourceType).USER, null, 'ADMIN')")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable("email") String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }
}
