package com.spotifyapp.repository;

import com.spotifyapp.model.entity.Playlist;
import com.spotifyapp.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    Optional<Playlist> findByIdAndIsDeletedFalse(Long id);
    Page<Playlist> findByOwnerAndIsDeletedFalse(User owner, Pageable pageable);
    List<Playlist> findByOwnerAndIsDeletedFalse(User owner);
    List<Playlist> findByOwnerAndIsSystemGeneratedTrueAndIsDeletedFalse(User owner);
}