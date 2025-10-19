package com.fzolv.shareware.hull.security;

import com.fzolv.shareware.data.entities.UserEntity;
import com.fzolv.shareware.data.entities.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class CustomUserDetails implements UserDetails {

    private final UUID userId;
    private final String email;
    private final UserRole userRole;

    public CustomUserDetails(UUID userId, String email, UserRole userRole) {
        this.userId = userId;
        this.email = email;
        this.userRole = userRole;
    }

    public static CustomUserDetails fromUserEntity(UserEntity userEntity) {
        return new CustomUserDetails(userEntity.getId(), userEntity.getEmail(), userEntity.getRole());
    }

    public UUID getUserId() {
        return userId;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = userRole == null ? "MEMBER" : userRole.name();
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName));
    }

    @Override
    public String getPassword() {
        return null; // Password not stored; authentication handled externally (e.g., OAuth2)
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}


