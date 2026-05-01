package com.pghpizza.api.application;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pghpizza.api.user.UserEntity;

public interface ContributorApplicationRepository extends JpaRepository<ContributorApplicationEntity, UUID> {
    List<ContributorApplicationEntity> findAllByOrderByCreatedAtDesc();

    boolean existsByApplicantUserAndStatus(UserEntity applicantUser, ApplicationStatus status);
}
