package com.spotifyapp.repository;

import com.spotifyapp.model.entity.User;
import com.spotifyapp.model.enums.UserRole;
import com.spotifyapp.model.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndIsDeletedFalse(String email);
    Optional<User> findByUsernameAndIsDeletedFalse(String username);
    Optional<User> findByIdAndIsDeletedFalse(Long id);
    boolean existsByEmailAndIsDeletedFalse(String email);
    boolean existsByUsernameAndIsDeletedFalse(String username);
    Page<User> findByIsDeletedFalse(Pageable pageable);
    Page<User> findByStatusAndIsDeletedFalse(UserStatus status, Pageable pageable);
    Page<User> findByRoleAndIsDeletedFalse(UserRole role, Pageable pageable);
    Page<User> findByRoleAndStatusAndIsDeletedFalse(UserRole role, UserStatus status, Pageable pageable);
    List<User> findByStatusAndVerificationCodeExpiryBefore(UserStatus status, LocalDateTime expiry);
    List<User> findByRoleAndIsDeletedFalse(UserRole role);
}

