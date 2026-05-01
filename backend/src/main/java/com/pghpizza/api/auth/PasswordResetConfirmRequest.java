package com.pghpizza.api.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(
        @NotBlank @Size(max = 256) String token,
        @NotBlank @Size(min = 8, max = 128) String password
) {
}
