package com.spotifyapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private String testEmail;
    private String testFirstName;
    private String testVerificationCode;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testFirstName = "John";
        testVerificationCode = "123456";
    }

    @Test
    void testSendVerificationEmail_Success() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendVerificationEmail(testEmail, testFirstName, testVerificationCode);

        // Assert
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertNotNull(sentMessage);
        assertEquals(testEmail, sentMessage.getTo()[0]);
        assertEquals("Verify Your Spotify Account", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains(testFirstName));
        assertTrue(sentMessage.getText().contains(testVerificationCode));
        assertTrue(sentMessage.getText().contains("This code will expire in 15 minutes"));
    }

    @Test
    void testSendVerificationEmail_WithNullFirstName() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendVerificationEmail(testEmail, null, testVerificationCode);

        // Assert
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertNotNull(sentMessage);
        assertTrue(sentMessage.getText().contains("Hello User"));
        assertTrue(sentMessage.getText().contains(testVerificationCode));
    }

    @Test
    void testSendVerificationEmail_MailSenderThrowsException() {
        // Arrange
        doThrow(new RuntimeException("Mail server error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                emailService.sendVerificationEmail(testEmail, testFirstName, testVerificationCode));

        assertEquals("Failed to send verification email", exception.getMessage());
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendVerificationEmail_EmailContentFormat() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendVerificationEmail(testEmail, testFirstName, testVerificationCode);

        // Assert
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        String emailText = sentMessage.getText();
        assertNotNull(emailText);
        assertTrue(emailText.contains("Hello " + testFirstName));
        assertTrue(emailText.contains("Your verification code is: " + testVerificationCode));
        assertTrue(emailText.contains("Best regards"));
        assertTrue(emailText.contains("Spotify Team"));
    }

    @Test
    void testSendVerificationEmail_VerifiesRecipient() {
        // Arrange
        String specificEmail = "specific.user@example.com";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendVerificationEmail(specificEmail, testFirstName, testVerificationCode);

        // Assert
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertEquals(specificEmail, sentMessage.getTo()[0]);
    }

    @Test
    void testSendVerificationEmail_VerifiesVerificationCode() {
        // Arrange
        String specificCode = "999888";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendVerificationEmail(testEmail, testFirstName, specificCode);

        // Assert
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertTrue(sentMessage.getText().contains(specificCode));
    }

    @Test
    void testSendVerificationEmail_HandlesEmptyFirstName() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendVerificationEmail(testEmail, "", testVerificationCode);

        // Assert
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        // Should default to "User" when firstName is empty
        assertTrue(sentMessage.getText().contains("Hello "));
    }

    @Test
    void testSendVerificationEmail_MultipleInvocations() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendVerificationEmail("user1@example.com", "User1", "111111");
        emailService.sendVerificationEmail("user2@example.com", "User2", "222222");
        emailService.sendVerificationEmail("user3@example.com", "User3", "333333");

        // Assert
        verify(mailSender, times(3)).send(any(SimpleMailMessage.class));
    }
}