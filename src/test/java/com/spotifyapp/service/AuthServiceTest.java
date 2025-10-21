package com.spotifyapp.service;

import com.spotifyapp.dto.auth.LoginRequest;
import com.spotifyapp.dto.auth.RegisterRequest;
import com.spotifyapp.exception.BadRequestException;
import com.spotifyapp.model.entity.User;
import com.spotifyapp.model.enums.UserRole;
import com.spotifyapp.model.enums.UserStatus;
import com.spotifyapp.repository.UserRepository;
import com.spotifyapp.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
        registerRequest.setRole("LISTENER");

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(UserRole.LISTENER)
                .status(UserStatus.PENDING)
                .verificationCode("123456")
                .verificationCodeExpiry(LocalDateTime.now().plusMinutes(15))
                .build();
    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.existsByEmailAndIsDeletedFalse(anyString())).thenReturn(false);
        when(userRepository.existsByUsernameAndIsDeletedFalse(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString(), anyString());

        authService.register(registerRequest);

        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testRegisterUser_DuplicateEmail() {
        when(userRepository.existsByEmailAndIsDeletedFalse(anyString())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testVerifyEmail_Success() {
        user.setStatus(UserStatus.PENDING);
        when(userRepository.findByEmailAndIsDeletedFalse(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.verifyEmail("test@example.com", "123456");

        verify(userRepository, times(1)).save(any(User.class));
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    void testVerifyEmail_InvalidCode() {
        when(userRepository.findByEmailAndIsDeletedFalse(anyString())).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () ->
                authService.verifyEmail("test@example.com", "wrong"));
    }

    @Test
    void testLogin_PendingStatus() {
        user.setStatus(UserStatus.PENDING);
        when(userRepository.findByEmailAndIsDeletedFalse(anyString())).thenReturn(Optional.of(user));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("Password123");

        assertThrows(BadRequestException.class, () -> authService.login(loginRequest));
    }
}
