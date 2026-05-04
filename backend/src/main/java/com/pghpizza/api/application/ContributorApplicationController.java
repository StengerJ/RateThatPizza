package com.pghpizza.api.application;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pghpizza.api.security.CurrentUserService;

import jakarta.validation.Valid;

@RestController
public class ContributorApplicationController {
    private final ContributorApplicationService applicationService;
    private final CurrentUserService currentUserService;

    public ContributorApplicationController(
            ContributorApplicationService applicationService,
            CurrentUserService currentUserService) {
        this.applicationService = applicationService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/api/applications")
    public ContributorApplicationResponse submitApplication(@Valid @RequestBody ContributorApplicationRequest request) {
        return applicationService.submitApplication(request);
    }

    @GetMapping("/api/admin/applications")
    @PreAuthorize("@currentUserService.isCurrentActiveAdmin()")
    public List<ContributorApplicationResponse> listApplications() {
        return applicationService.listApplications();
    }

    @PostMapping("/api/admin/applications/{id}/approve")
    @PreAuthorize("@currentUserService.isCurrentActiveAdmin()")
    public ContributorApplicationResponse approve(@PathVariable UUID id) {
        return applicationService.approve(id, currentUserService.requireCurrentUser());
    }

    @PostMapping("/api/admin/applications/{id}/reject")
    @PreAuthorize("@currentUserService.isCurrentActiveAdmin()")
    public ContributorApplicationResponse reject(@PathVariable UUID id) {
        return applicationService.reject(id, currentUserService.requireCurrentUser());
    }
}
