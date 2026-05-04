package com.pghpizza.api.user;

import java.time.Instant;
import java.util.UUID;

public record UserAdminResponse(
        UUID id,
        String email,
        String displayName,
        UserRole role,
        UserStatus status,
        Instant createdAt,
        boolean protectedAdmin
) {
    public static UserAdminResponse from(UserEntity user) {
        return from(user, false);
    }

    public static UserAdminResponse from(UserEntity user, boolean protectedAdmin) {
        return new UserAdminResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                protectedAdmin);
    }
}
