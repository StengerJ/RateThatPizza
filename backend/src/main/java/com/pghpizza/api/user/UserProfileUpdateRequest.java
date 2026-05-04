package com.pghpizza.api.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @NotBlank @Size(min = 2, max = 120) String displayName,
        @Size(max = 500) String bio,
        @Size(max = 500) String profilePictureUrl
) {
}
