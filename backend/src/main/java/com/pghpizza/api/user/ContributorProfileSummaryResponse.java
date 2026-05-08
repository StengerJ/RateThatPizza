package com.pghpizza.api.user;

import java.util.UUID;

public record ContributorProfileSummaryResponse(
        UUID id,
        String displayName,
        String profilePictureUrl,
        long ratingCount,
        long blogPostCount
) {
    public static ContributorProfileSummaryResponse from(
            UserEntity user,
            long ratingCount,
            long blogPostCount) {
        return new ContributorProfileSummaryResponse(
                user.getId(),
                user.getDisplayName(),
                user.getProfilePictureUrl(),
                ratingCount,
                blogPostCount);
    }
}
