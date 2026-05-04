package com.pghpizza.api.user;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pghpizza.api.common.ConflictException;
import com.pghpizza.api.common.NotFoundException;

@Service
public class UserAdminService {
    private static final List<UserRole> MANAGED_ROLES = List.of(UserRole.ADMIN, UserRole.CONTRIBUTOR);

    private final UserRepository userRepository;

    public UserAdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserAdminResponse> listUsers() {
        return userRepository.findAllByStatusAndRoleInOrderByDisplayNameAsc(UserStatus.ACTIVE, MANAGED_ROLES)
                .stream()
                .map(UserAdminResponse::from)
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

        user.setRole(requestedRole);
        user.setStatus(UserStatus.ACTIVE);

        return UserAdminResponse.from(userRepository.save(user));
    }
}
