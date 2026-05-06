package com.pghpizza.api.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pghpizza.api.common.ConflictException;
import com.pghpizza.api.common.NotFoundException;
import com.pghpizza.api.common.TextSanitizer;
import com.pghpizza.api.email.EmailService;
import com.pghpizza.api.user.UserEntity;
import com.pghpizza.api.user.UserRepository;
import com.pghpizza.api.user.UserRole;
import com.pghpizza.api.user.UserStatus;

@Service
public class ContributorApplicationService {
    private final ContributorApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public ContributorApplicationService(
            ContributorApplicationRepository applicationRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public ContributorApplicationResponse submitApplication(ContributorApplicationRequest request) {
        String email = TextSanitizer.normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("An account already exists for this email");
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setDisplayName(TextSanitizer.trim(request.displayName()));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.PENDING_CONTRIBUTOR);
        user.setStatus(UserStatus.PENDING);
        userRepository.save(user);

        ContributorApplicationEntity application = new ContributorApplicationEntity();
        application.setApplicantUser(user);
        application.setApplicationReason(TextSanitizer.trim(request.applicationReason()));
        application.setStatus(ApplicationStatus.PENDING);

        return ContributorApplicationResponse.from(applicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public List<ContributorApplicationResponse> listApplications() {
        return applicationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ContributorApplicationResponse::from)
                .toList();
    }

    @Transactional
    public ContributorApplicationResponse approve(UUID id, UserEntity reviewer) {
        ContributorApplicationEntity application = requirePendingApplication(id);
        UserEntity applicant = application.getApplicantUser();
        applicant.setRole(UserRole.CONTRIBUTOR);
        applicant.setStatus(UserStatus.ACTIVE);
        application.setStatus(ApplicationStatus.APPROVED);
        application.setReviewedBy(reviewer);
        application.setReviewedAt(Instant.now());

        userRepository.save(applicant);
        ContributorApplicationEntity savedApplication = applicationRepository.save(application);
        emailService.sendContributorApprovalEmail(applicant.getEmail(), applicant.getDisplayName());
        return ContributorApplicationResponse.from(savedApplication);
    }

    @Transactional
    public ContributorApplicationResponse reject(UUID id, UserEntity reviewer) {
        ContributorApplicationEntity application = requirePendingApplication(id);
        UserEntity applicant = application.getApplicantUser();
        applicant.setRole(UserRole.PENDING_CONTRIBUTOR);
        applicant.setStatus(UserStatus.REJECTED);
        application.setStatus(ApplicationStatus.REJECTED);
        application.setReviewedBy(reviewer);
        application.setReviewedAt(Instant.now());

        userRepository.save(applicant);
        ContributorApplicationEntity savedApplication = applicationRepository.save(application);
        emailService.sendContributorRejectionEmail(applicant.getEmail(), applicant.getDisplayName());
        return ContributorApplicationResponse.from(savedApplication);
    }

    private ContributorApplicationEntity requirePendingApplication(UUID id) {
        ContributorApplicationEntity application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new ConflictException("Application has already been reviewed");
        }

        return application;
    }
}
