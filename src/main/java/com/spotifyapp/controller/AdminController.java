package com.spotifyapp.controller;

import com.spotifyapp.dto.ApiResponse;
import com.spotifyapp.dto.user.UpdateUserRequest;
import com.spotifyapp.dto.user.UserResponse;
import com.spotifyapp.model.enums.UserRole;
import com.spotifyapp.model.enums.UserStatus;
import com.spotifyapp.service.AlbumService;
import com.spotifyapp.service.MusicService;
import com.spotifyapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin-only endpoints for user, music, and album management")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final MusicService musicService;
    private final AlbumService albumService;

    // User Management
    @Operation(
            summary = "Get all users",
            description = "Get paginated list of all users (ADMIN only)"
    )
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "Get users by status",
            description = "Get paginated list of users filtered by status (ADMIN only)"
    )
    @GetMapping("/users/status/{status}")
    public ResponseEntity<Page<UserResponse>> getUsersByStatus(
            @Parameter(description = "User status", example = "ACTIVE") @PathVariable UserStatus status,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = userService.getUsersByStatus(status, pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "Get users by role",
            description = "Get paginated list of users filtered by role (ADMIN only)"
    )
    @GetMapping("/users/role/{role}")
    public ResponseEntity<Page<UserResponse>> getUsersByRole(
            @Parameter(description = "User role", example = "ARTIST") @PathVariable UserRole role,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = userService.getUsersByRole(role, pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "Get user by ID",
            description = "Get user details by ID (ADMIN only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "Update user",
            description = "Update user details (ADMIN only)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Username or email already taken"
            )
    })
    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User update details",
                    content = @Content(
                            schema = @Schema(implementation = UpdateUserRequest.class),
                            examples = @ExampleObject(value = """
                                {
                                  "username": "new_username",
                                  "email": "newemail@example.com",
                                  "firstName": "John",
                                  "lastName": "Doe"
                                }
                                """)
                    )
            )
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "Block user",
            description = "Block user account (ADMIN only)"
    )
    @PostMapping("/users/{id}/block")
    public ResponseEntity<ApiResponse> blockUser(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        userService.blockUser(id);
        return ResponseEntity.ok(new ApiResponse("User blocked successfully"));
    }

    @Operation(
            summary = "Unblock user",
            description = "Unblock user account (ADMIN only)"
    )
    @PostMapping("/users/{id}/unblock")
    public ResponseEntity<ApiResponse> unblockUser(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        userService.unblockUser(id);
        return ResponseEntity.ok(new ApiResponse("User unblocked successfully"));
    }

    @Operation(
            summary = "Delete user",
            description = "Soft delete user (ADMIN only)"
    )
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse> deleteUser(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse("User deleted successfully"));
    }

    // Music Management
    @Operation(
            summary = "Delete music (admin)",
            description = "Admin can delete any music track (ADMIN only)"
    )
    @DeleteMapping("/music/{id}")
    public ResponseEntity<ApiResponse> deleteMusic(
            @Parameter(description = "Music ID", example = "1") @PathVariable Long id) {
        musicService.deleteMusicByAdmin(id);
        return ResponseEntity.ok(new ApiResponse("Music deleted successfully"));
    }

    // Album Management
    @Operation(
            summary = "Delete album (admin)",
            description = "Admin can delete any album (ADMIN only)"
    )
    @DeleteMapping("/albums/{id}")
    public ResponseEntity<ApiResponse> deleteAlbum(
            @Parameter(description = "Album ID", example = "1") @PathVariable Long id) {
        albumService.deleteAlbumByAdmin(id);
        return ResponseEntity.ok(new ApiResponse("Album deleted successfully"));
    }
}