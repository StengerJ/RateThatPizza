package com.pghpizza.api.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.pghpizza.api.common.NotFoundException;
import com.pghpizza.api.user.UserEntity;
import com.pghpizza.api.user.UserRepository;
import com.pghpizza.api.user.UserRole;
import com.pghpizza.api.user.UserStatus;

@Service
public class CurrentUserService {
    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity requireCurrentUser() {
        return currentUser().orElseThrow(() -> new NotFoundException("User not found"));
    }

    public Optional<UserEntity> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        try {
            UUID userId = UUID.fromString(authentication.getName());
            return userRepository.findById(userId);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public boolean isAdmin(UserEntity user) {
        return user.getRole() == UserRole.ADMIN;
    }

    public boolean isCurrentActiveAdmin() {
        return currentUser()
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .filter(user -> user.getRole() == UserRole.ADMIN)
                .isPresent();
    }
}
