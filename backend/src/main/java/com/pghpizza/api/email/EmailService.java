package com.pghpizza.api.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pghpizza.api.config.AppProperties;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final AppProperties properties;
    private final JavaMailSender mailSender;

    public EmailService(AppProperties properties, JavaMailSender mailSender) {
        this.properties = properties;
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String recipient, String resetLink) {
        if (!properties.mail().smtpEnabled() || !StringUtils.hasText(properties.mail().from())) {
            log.info("Development password reset link for {}: {}", recipient, resetLink);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(properties.mail().from());
            message.setTo(recipient);
            message.setSubject("Reset your PGH-Pizza password");
            message.setText("Use this link to reset your PGH-Pizza password:\n\n" + resetLink
                    + "\n\nThis link expires in 30 minutes.");
            mailSender.send(message);
        } catch (RuntimeException exception) {
            log.error("SMTP password reset email failed for {}; using development log fallback", recipient, exception);
            log.info("Development password reset link for {}: {}", recipient, resetLink);
        }
    }
}
