package com.pghpizza.api.user;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pghpizza.api.common.ConflictException;
import com.pghpizza.api.common.NotFoundException;
import com.pghpizza.api.common.TextSanitizer;
import com.pghpizza.api.config.AppProperties;

@Service
public class UserAdminService {
    private static final List<UserRole> MANAGED_ROLES = List.of(UserRole.ADMIN, UserRole.CONTRIBUTOR);

    private final UserRepository userRepository;
    private final AppProperties properties;

    public UserAdminService(UserRepository userRepository, AppProperties properties) {
        this.userRepository = userRepository;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public List<UserAdminResponse> listUsers() {
        return userRepository.findAllByStatusAndRoleInOrderByDisplayNameAsc(UserStatus.ACTIVE, MANAGED_ROLES)
                .stream()
                .map(user -> UserAdminResponse.from(user, isProtectedAdmin(user)))
                .toList();
    }

    @Transactional
    public UserAdminResponse updateRole(UUID id, UserAdminRoleUpdateRequest request, UserEntity currentAdmin) {
        UserRole requestedRole = request.role();
        if (!MANAGED_ROLES.contains(requestedRole)) {
            throw new ConflictException("Role must be ADMIN or CONTRIBUTOR");
        }

        if (currentAdmin.getId().equals(id)) {
            throw new ConflictException("You cannot change your own role");
        }

        UserEntity user = userRepository.findById(id)
                .filter(candidate -> candidate.getStatus() == UserStatus.ACTIVE)
                .filter(candidate -> MANAGED_ROLES.contains(candidate.getRole()))
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (isProtectedAdmin(user) && requestedRole != UserRole.ADMIN) {
            throw new ConflictException("The default admin account cannot be demoted");
        }

        user.setRole(requestedRole);
        user.setStatus(UserStatus.ACTIVE);

        UserEntity savedUser = userRepository.save(user);
        return UserAdminResponse.from(savedUser, isProtectedAdmin(savedUser));
    }

    private boolean isProtectedAdmin(UserEntity user) {
        return TextSanitizer.normalizeEmail(properties.admin().email()).equals(user.getEmail());
    }
}
