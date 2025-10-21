package com.spotifyapp.service;

import com.spotifyapp.dto.user.UpdateUserRequest;
import com.spotifyapp.dto.user.UserResponse;
import com.spotifyapp.exception.BadRequestException;
import com.spotifyapp.exception.ResourceNotFoundException;
import com.spotifyapp.model.entity.User;
import com.spotifyapp.model.enums.UserRole;
import com.spotifyapp.model.enums.UserStatus;
import com.spotifyapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.LISTENER)
                .status(UserStatus.ACTIVE)
                .isDeleted(false)
                .build();
    }

    @Test
    void testGetUserById_Success() {
        when(userRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    void testUpdateUser_Success() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("newusername");
        request.setFirstName("New");

        when(userRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndIsDeletedFalse(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.updateUser(1L, request);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUser_DuplicateUsername() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("existinguser");

        when(userRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndIsDeletedFalse(anyString())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.updateUser(1L, request));
    }

    @Test
    void testBlockUser_Success() {
        when(userRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.blockUser(1L);

        verify(userRepository, times(1)).save(any(User.class));
        assertEquals(UserStatus.BLOCKED, user.getStatus());
    }

    @Test
    void testUnblockUser_Success() {
        user.setStatus(UserStatus.BLOCKED);
        when(userRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.unblockUser(1L);

        verify(userRepository, times(1)).save(any(User.class));
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    void testDeleteUser_Success() {
        when(userRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).save(any(User.class));
        assertTrue(user.getIsDeleted());
    }
}
