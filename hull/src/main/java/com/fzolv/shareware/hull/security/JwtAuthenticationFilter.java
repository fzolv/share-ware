package com.fzolv.shareware.hull.security;

import com.fzolv.shareware.data.entities.GroupMemberEntity;
import com.fzolv.shareware.data.repositories.GroupMemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final GroupMemberRepository groupMemberRepository;

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(JwtService jwtService, GroupMemberRepository groupMemberRepository) {
        this.jwtService = jwtService;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            log.debug("JWT filter ENTER uri={} authHeader={}", request.getRequestURI(), request.getHeader("Authorization") != null);
            String token = extractTokenFromAuthorizationHeader(request);
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Map<String, Object> claims = jwtService.verifyAndParse(token);
                String email = (String) claims.get("sub");
                String role = (String) claims.get("role");
                if (email != null && role != null) {
                    String effectiveRole = role;
                    UUID userId = parseUuid((String) claims.get("uid"));
                    UUID groupId = extractGroupId(request);
                    if (userId != null && groupId != null) {
                        Optional<GroupMemberEntity> membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
                        if (membership.isPresent() && membership.get().getRole() != null) {
                            effectiveRole = membership.get().getRole().name();
                        }
                    }
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + effectiveRole))
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("JWT filter SUCCESS userId={} role={} uri={}", userId, effectiveRole, request.getRequestURI());
                }
            }
        } catch (Exception ex) {
            log.warn("JWT filter FAIL uri={} error={}", request.getRequestURI(), ex.getMessage(), ex);
        }

        filterChain.doFilter(request, response);
    }

    private static String extractTokenFromAuthorizationHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null) {
            return null;
        }
        if (header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private static UUID extractGroupId(HttpServletRequest request) {
        String param = request.getParameter("groupId");
        UUID parsed = parseUuid(param);
        if (parsed != null) {
            return parsed;
        }
        String uri = request.getRequestURI();
        Pattern pattern = Pattern.compile("/groups/([0-9a-fA-F-]{36})");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            return parseUuid(matcher.group(1));
        }
        return null;
    }

    private static UUID parseUuid(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}


