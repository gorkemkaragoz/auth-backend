package com.gorkem.auth_service.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mailService, "fromEmail", "noreply@authservice.com");
    }

    @Test
    @DisplayName("OTP maili dogru alici ve icerikle gonderilmeli")
    void shouldSendOtpMailWithCorrectDetails() {
        // Given
        String to = "user@example.com";
        String otp = "123456";
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        mailService.sendOtpMail(to, otp);

        // Then
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getTo()).contains(to);
        assertThat(sentMessage.getFrom()).isEqualTo("noreply@authservice.com");
        assertThat(sentMessage.getText()).contains(otp);
        assertThat(sentMessage.getSubject()).isNotNull();
    }

    @Test
    @DisplayName("Mail gonderimi sirasinda exception firlatilirsa yukari tasimali")
    void shouldPropagateExceptionWhenMailSendFails() {
        // Given
        doThrow(new RuntimeException("SMTP connection failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> mailService.sendOtpMail("user@example.com", "123456")
                ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SMTP connection failed");
    }
}