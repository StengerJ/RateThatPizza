package com.pghpizza.api.user;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String displayName,
        String profileBio,
        String profilePictureUrl,
        UserRole role,
        UserStatus status
) {
    public static UserResponse from(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getProfileBio(),
                user.getProfilePictureUrl(),
                user.getRole(),
                user.getStatus());
    }
}
