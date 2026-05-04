package com.pghpizza.api.user;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pghpizza.api.security.CurrentUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("@currentUserService.isCurrentActiveAdmin()")
public class UserAdminController {
    private final UserAdminService userAdminService;
    private final CurrentUserService currentUserService;

    public UserAdminController(UserAdminService userAdminService, CurrentUserService currentUserService) {
        this.userAdminService = userAdminService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<UserAdminResponse> listUsers() {
        return userAdminService.listUsers();
    }

    @PutMapping("/{id}/role")
    public UserAdminResponse updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody UserAdminRoleUpdateRequest request) {
        return userAdminService.updateRole(id, request, currentUserService.requireCurrentUser());
    }
}
