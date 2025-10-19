package com.fzolv.shareware.hull.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("resourceAuth")
public class ResourceAuthorizationEvaluator {

    private final ResourceAuthorizationService resourceAuthorizationService;

    public ResourceAuthorizationEvaluator(ResourceAuthorizationService resourceAuthorizationService) {
        this.resourceAuthorizationService = resourceAuthorizationService;
    }

    public boolean hasRole(ResourceType resourceType, String resourceId, String... allowedRoles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        Set<String> allowed = new HashSet<>(Arrays.asList(allowedRoles));

        // Fast-path: allow global ADMIN if requested
        if (allowed.contains("ADMIN") && hasAuthority(authentication, "ROLE_ADMIN")) {
            return true;
        }

        UUID parsedId = parseUuid(resourceId);
        Optional<String> roleOpt = resourceAuthorizationService.resolveRole(resourceType, authentication, parsedId);

        if (roleOpt.isEmpty()) {
            return false;
        }

        String resolvedRole = roleOpt.get();
        if (!allowed.contains(resolvedRole)) {
            return false;
        }

        // Enforce self-or-admin semantics for USER resources: members can only access their own userId
        if (resourceType == ResourceType.USER && "MEMBER".equals(resolvedRole)) {
            UUID authUserId = extractUserId(authentication);
            return authUserId != null && parsedId != null && parsedId.equals(authUserId);
        }

        return true;
    }

    private static boolean hasAuthority(Authentication authentication, String authority) {
        for (GrantedAuthority granted : authentication.getAuthorities()) {
            if (authority.equals(granted.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private static UUID parseUuid(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static UUID extractUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof java.util.UUID) {
            return (java.util.UUID) principal;
        }
        if (principal instanceof com.fzolv.shareware.hull.security.CustomUserDetails) {
            return ((com.fzolv.shareware.hull.security.CustomUserDetails) principal).getUserId();
        }
        if (principal instanceof String) {
            try {
                return java.util.UUID.fromString((String) principal);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
        return null;
    }
}


