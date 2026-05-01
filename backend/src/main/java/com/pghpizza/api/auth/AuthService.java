package com.pghpizza.api.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pghpizza.api.common.ConflictException;
import com.pghpizza.api.common.TextSanitizer;
import com.pghpizza.api.config.AppProperties;
import com.pghpizza.api.email.EmailService;
import com.pghpizza.api.user.UserEntity;
import com.pghpizza.api.user.UserRepository;
import com.pghpizza.api.user.UserResponse;
import com.pghpizza.api.user.UserStatus;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final TokenHashingService tokenHashingService;
    private final EmailService emailService;
    private final AppProperties properties;

    public AuthService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            TokenHashingService tokenHashingService,
            EmailService emailService,
            AppProperties properties) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.tokenHashingService = tokenHashingService;
        this.emailService = emailService;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(TextSanitizer.normalizeEmail(request.email()))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (user.getStatus() == UserStatus.DISABLED || user.getStatus() == UserStatus.REJECTED
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return new AuthResponse(createToken(user), UserResponse.from(user));
    }

    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) {
        userRepository.findByEmail(TextSanitizer.normalizeEmail(request.email()))
                .filter(user -> user.getStatus() != UserStatus.DISABLED && user.getStatus() != UserStatus.REJECTED)
                .ifPresent(user -> {
                    String rawToken = tokenHashingService.newRawToken();
                    PasswordResetTokenEntity token = new PasswordResetTokenEntity();
                    token.setUser(user);
                    token.setTokenHash(tokenHashingService.sha256(rawToken));
                    token.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
                    tokenRepository.save(token);

                    String link = properties.frontendBaseUrl() + "/password-reset/confirm?token=" + rawToken;
                    emailService.sendPasswordResetEmail(user.getEmail(), link);
                });
    }

    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        String tokenHash = tokenHashingService.sha256(request.token());
        PasswordResetTokenEntity token = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ConflictException("Reset token is invalid or expired"));

        Instant now = Instant.now();
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(now)) {
            throw new ConflictException("Reset token is invalid or expired");
        }

        UserEntity user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        token.setUsedAt(now);
        userRepository.save(user);
        tokenRepository.save(token);
    }

    private String createToken(UserEntity user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.jwt().expiresMinutes(), ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.jwt().issuer())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("displayName", user.getDisplayName())
                .claim("role", user.getRole().name())
                .claim("status", user.getStatus().name())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
