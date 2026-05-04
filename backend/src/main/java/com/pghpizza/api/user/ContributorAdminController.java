package com.pghpizza.api.user;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/contributors")
@PreAuthorize("@currentUserService.isCurrentActiveAdmin()")
public class ContributorAdminController {
    private final ContributorAdminService contributorAdminService;

    public ContributorAdminController(ContributorAdminService contributorAdminService) {
        this.contributorAdminService = contributorAdminService;
    }

    @GetMapping
    public List<ContributorAdminResponse> listContributors() {
        return contributorAdminService.listContributors();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disableContributor(@PathVariable UUID id) {
        contributorAdminService.disableContributor(id);
        return ResponseEntity.noContent().build();
    }
}
