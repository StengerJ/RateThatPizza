package com.pghpizza.api.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.pghpizza.api.config.AppProperties;

class EmailServiceTests {
    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final EmailService emailService = new EmailService(
            new AppProperties(
                    null,
                    null,
                    null,
                    new AppProperties.Mail("hello@pghpizza.org", true),
                    null),
            mailSender);

    @Test
    void contributorApprovalEmailUsesAcceptedTone() {
        emailService.sendContributorApprovalEmail("approved@example.com", "Approved Person");

        SimpleMailMessage message = sentMessage();
        assertThat(message.getTo()).containsExactly("approved@example.com");
        assertThat(message.getSubject()).isEqualTo("Your pghpizza.org contributor application was approved");
        assertThat(message.getText()).isEqualTo("""
                Hello Approved Person,

                Thank you for taking the time to apply to be a contributor to pghpizza.org!
                We appreciate your dedication to pizza very much!

                Your contributor application has been approved. You can now log in and start sharing your pizza ratings and blog posts with the Pittsburgh pizza community.""");
    }

    @Test
    void contributorRejectionEmailUsesDeniedTone() {
        emailService.sendContributorRejectionEmail("denied@example.com", "Denied Person");

        SimpleMailMessage message = sentMessage();
        assertThat(message.getTo()).containsExactly("denied@example.com");
        assertThat(message.getSubject()).isEqualTo("Your pghpizza.org contributor application update");
        assertThat(message.getText()).isEqualTo("""
                Hello Denied Person,

                Thank you for taking the time to apply to be a contributor to pghpizza.org!
                We appreciate your dedication to pizza very much!

                We are not able to approve your contributor application at this time, but we still appreciate your interest in being part of the Pittsburgh pizza community.""");
    }

    private SimpleMailMessage sentMessage() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }
}
