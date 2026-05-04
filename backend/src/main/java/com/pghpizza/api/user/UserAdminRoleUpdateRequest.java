package com.pghpizza.api.user;

import jakarta.validation.constraints.NotNull;

public record UserAdminRoleUpdateRequest(
        @NotNull UserRole role
) {
}
