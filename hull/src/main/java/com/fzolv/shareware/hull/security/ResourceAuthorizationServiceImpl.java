package com.fzolv.shareware.hull.security;

import com.fzolv.shareware.data.entities.ExpenseEntity;
import com.fzolv.shareware.data.repositories.ExpenseRepository;
import com.fzolv.shareware.data.repositories.GroupMemberRepository;
import com.fzolv.shareware.data.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ResourceAuthorizationServiceImpl implements ResourceAuthorizationService {

    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ExpenseRepository expenseRepository;

    public ResourceAuthorizationServiceImpl(UserRepository userRepository, GroupMemberRepository groupMemberRepository, ExpenseRepository expenseRepository) {
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.expenseRepository = expenseRepository;
    }

    @Override
    public Optional<String> resolveRole(ResourceType resourceType, Authentication authentication, UUID resourceId) {
        UUID userId = extractUserId(authentication);
        if (userId == null) {
            return Optional.empty();
        }

        switch (resourceType) {
            case USER:
                return userRepository.findById(userId).map(user -> user.getRole().name());
            case GROUP:
                return resolveGroupScopedRole(userId, resourceId);
            case EXPENSE:
                return resolveExpenseScopedRole(userId, resourceId);
            default:
                return Optional.empty();
        }
    }

    private Optional<String> resolveExpenseScopedRole(UUID userId, UUID expenseId) {
        if (expenseId == null) {
            return Optional.empty();
        }
        Optional<ExpenseEntity> expense = expenseRepository.findById(expenseId);
        if (expense.isEmpty() || expense.get().getGroup() == null) {
            return Optional.empty();
        }
        UUID groupId = expense.get().getGroup().getId();
        return resolveGroupScopedRole(userId, groupId);
    }

    private Optional<String> resolveGroupScopedRole(UUID userId, UUID groupId) {
        if (groupId == null) {
            return Optional.empty();
        }
        return groupMemberRepository
                .findByGroupIdAndUserId(groupId, userId)
                .map(member -> member.getRole().name());
    }

    private static UUID extractUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UUID) {
            return (UUID) principal;
        }
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        }
        if (principal instanceof String) {
            try {
                return UUID.fromString((String) principal);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
        return null;
    }
}


