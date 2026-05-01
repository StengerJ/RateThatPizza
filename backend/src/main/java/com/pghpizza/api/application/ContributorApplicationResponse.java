package com.pghpizza.api.application;

import java.time.Instant;
import java.util.UUID;

public record ContributorApplicationResponse(
        UUID id,
        String email,
        String displayName,
        String applicationReason,
        ApplicationStatus status,
        Instant createdAt
) {
    public static ContributorApplicationResponse from(ContributorApplicationEntity application) {
        return new ContributorApplicationResponse(
                application.getId(),
                application.getApplicantUser().getEmail(),
                application.getApplicantUser().getDisplayName(),
                application.getApplicationReason(),
                application.getStatus(),
                application.getCreatedAt());
    }
}
