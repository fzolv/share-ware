package com.fzolv.shareware.hull.security;

import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

public interface ResourceAuthorizationService {

    Optional<String> resolveRole(ResourceType resourceType, Authentication authentication, UUID resourceId);
}


