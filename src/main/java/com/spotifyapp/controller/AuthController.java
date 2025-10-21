package com.spotifyapp.controller;

import com.spotifyapp.dto.auth.AuthResponse;
import com.spotifyapp.dto.auth.LoginRequest;
import com.spotifyapp.dto.auth.RegisterRequest;
import com.spotifyapp.dto.auth.VerifyEmailRequest;
import com.spotifyapp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register new user",
            description = "Register a new user as LISTENER or ARTIST. Email verification required. " +
                    "A 6-digit verification code will be sent to the provided email address."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Registration successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.spotifyapp.dto.ApiResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Registration successful. Please verify your email.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or user already exists",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Email already registered\"}")
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<com.spotifyapp.dto.ApiResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(value = """
                                {
                                  "username": "john_doe",
                                  "email": "john@example.com",
                                  "password": "SecurePass123",
                                  "firstName": "John",
                                  "lastName": "Doe",
                                  "role": "LISTENER"
                                }
                                """)
                    )
            )
            @Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new com.spotifyapp.dto.ApiResponse("Registration successful. Please verify your email."));
    }

    @Operation(
            summary = "Verify email",
            description = "Verify user email with 6-digit code sent to email. " +
                    "Code expires in 15 minutes."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Email verified successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.spotifyapp.dto.ApiResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Email verified successfully\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired verification code",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Invalid verification code\"}")
                    )
            )
    })
    @PostMapping("/verify-email")
    public ResponseEntity<com.spotifyapp.dto.ApiResponse> verifyEmail(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Email verification details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = VerifyEmailRequest.class),
                            examples = @ExampleObject(value = """
                                {
                                  "email": "john@example.com",
                                  "verificationCode": "123456"
                                }
                                """)
                    )
            )
            @Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.getEmail(), request.getVerificationCode());
        return ResponseEntity.ok(new com.spotifyapp.dto.ApiResponse("Email verified successfully"));
    }

    @Operation(
            summary = "Login",
            description = "Authenticate user and receive JWT token. Account must be verified and active."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(value = """
                                {
                                  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                  "userId": 1,
                                  "username": "john_doe",
                                  "role": "LISTENER"
                                }
                                """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid credentials or account not verified",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Please verify your email first\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Account blocked",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Your account has been blocked\"}")
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User login credentials",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(value = """
                                {
                                  "email": "john@example.com",
                                  "password": "SecurePass123"
                                }
                                """)
                    )
            )
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}