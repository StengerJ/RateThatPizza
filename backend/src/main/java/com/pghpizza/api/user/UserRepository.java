package com.pghpizza.api.user;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);

    List<UserEntity> findAllByRoleAndStatusOrderByDisplayNameAsc(UserRole role, UserStatus status);

    List<UserEntity> findAllByStatusAndRoleInOrderByDisplayNameAsc(UserStatus status, Collection<UserRole> roles);

    boolean existsByEmail(String email);
}
