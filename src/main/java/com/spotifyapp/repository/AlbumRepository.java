package com.spotifyapp.repository;

import com.spotifyapp.model.entity.Album;
import com.spotifyapp.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    Optional<Album> findByIdAndIsDeletedFalse(Long id);
    Page<Album> findByIsDeletedFalse(Pageable pageable);
    List<Album> findByArtistAndIsDeletedFalse(User artist);
    Page<Album> findByArtistAndIsDeletedFalse(User artist, Pageable pageable);
    long countByArtistAndIsDeletedFalse(User artist);
}
