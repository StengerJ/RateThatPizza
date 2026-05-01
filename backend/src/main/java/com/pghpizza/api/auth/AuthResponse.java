package com.pghpizza.api.auth;

import com.pghpizza.api.user.UserResponse;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
