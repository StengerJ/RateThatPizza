package com.pghpizza.api.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.pghpizza.api.common.NotFoundException;
import com.pghpizza.api.user.UserEntity;
import com.pghpizza.api.user.UserRepository;
import com.pghpizza.api.user.UserRole;

@Service
public class CurrentUserService {
    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(authentication.getName());
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    }

    public boolean isAdmin(UserEntity user) {
        return user.getRole() == UserRole.ADMIN;
    }
}
