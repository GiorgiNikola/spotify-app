package com.spotifyapp.service;

import com.spotifyapp.dto.auth.AuthResponse;
import com.spotifyapp.dto.auth.LoginRequest;
import com.spotifyapp.dto.auth.RegisterRequest;
import com.spotifyapp.exception.BadRequestException;
import com.spotifyapp.model.entity.User;
import com.spotifyapp.model.enums.UserRole;
import com.spotifyapp.model.enums.UserStatus;
import com.spotifyapp.repository.UserRepository;
import com.spotifyapp.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public void register(RegisterRequest request) {
        // Check if user exists
        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        if (userRepository.existsByUsernameAndIsDeletedFalse(request.getUsername())) {
            throw new BadRequestException("Username already taken");
        }

        // Generate verification code
        String verificationCode = generateVerificationCode();

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(UserRole.valueOf(request.getRole()))
                .status(UserStatus.PENDING)
                .verificationCode(verificationCode)
                .verificationCodeExpiry(LocalDateTime.now().plusMinutes(15))
                .build();

        userRepository.save(user);

        // Send verification email
        emailService.sendVerificationEmail(
                user.getEmail(),
                user.getFirstName(),
                verificationCode
        );

        log.info("User registered successfully: {}", user.getEmail());
    }

    @Transactional
    public void verifyEmail(String email, String verificationCode) {
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new BadRequestException("Email already verified");
        }

        if (user.getVerificationCode() == null ||
                !user.getVerificationCode().equals(verificationCode)) {
            throw new BadRequestException("Invalid verification code");
        }

        if (user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification code expired");
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);

        userRepository.save(user);

        log.info("Email verified successfully: {}", email);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (user.getStatus() == UserStatus.PENDING) {
            throw new BadRequestException("Please verify your email first");
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new BadRequestException("Your account has been blocked");
        }

        // Authenticate
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        request.getPassword()
                )
        );

        // Generate token
        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getId()
        );

        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
