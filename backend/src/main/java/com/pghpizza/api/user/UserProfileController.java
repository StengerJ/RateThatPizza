package com.pghpizza.api.user;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pghpizza.api.security.CurrentUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profiles")
public class UserProfileController {
    private final UserProfileService userProfileService;
    private final CurrentUserService currentUserService;

    public UserProfileController(UserProfileService userProfileService, CurrentUserService currentUserService) {
        this.userProfileService = userProfileService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/{id}")
    public UserProfileResponse getProfile(@PathVariable UUID id) {
        return userProfileService.getProfile(id);
    }

    @PutMapping("/me")
    public UserProfileResponse updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        return userProfileService.updateProfile(request, currentUserService.requireCurrentUser());
    }
}
