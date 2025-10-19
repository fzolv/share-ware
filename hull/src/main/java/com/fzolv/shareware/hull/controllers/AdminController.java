package com.fzolv.shareware.hull.controllers;

import com.fzolv.shareware.data.repositories.UserRepository;
import com.fzolv.shareware.hull.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admins")
public class AdminController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AdminController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/tokens/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> issueTokenForUser(@PathVariable("userId") String userId,
                                                                 @RequestParam(value = "ttlSeconds", required = false, defaultValue = "3600") long ttlSeconds) {
        UUID id;
        try {
            id = UUID.fromString(userId);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error("Invalid userId"));
        }

        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(token(jwtService.issueToken(user, ttlSeconds))))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("User not found")));
    }

    private static Map<String, String> token(String token) {
        Map<String, String> map = new HashMap<>();
        map.put("accessToken", token);
        return map;
    }

    private static Map<String, String> error(String message) {
        Map<String, String> map = new HashMap<>();
        map.put("error", message);
        return map;
    }
}


