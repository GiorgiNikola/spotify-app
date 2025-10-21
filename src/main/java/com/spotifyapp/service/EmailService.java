package com.spotifyapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String firstName, String verificationCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Verify Your Spotify Account");
            message.setText(String.format(
                    "Hello %s,\n\n" +
                            "Your verification code is: %s\n\n" +
                            "This code will expire in 15 minutes.\n\n" +
                            "If you didn't create this account, please ignore this email.\n\n" +
                            "Best regards,\n" +
                            "Spotify Team",
                    firstName != null ? firstName : "User",
                    verificationCode
            ));

            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email");
        }
    }
}
