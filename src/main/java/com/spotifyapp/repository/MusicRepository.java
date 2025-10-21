package com.spotifyapp.repository;

import com.spotifyapp.model.entity.Album;
import com.spotifyapp.model.entity.Music;
import com.spotifyapp.model.entity.User;
import com.spotifyapp.model.enums.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {
    Optional<Music> findByIdAndIsDeletedFalse(Long id);
    Page<Music> findByIsDeletedFalse(Pageable pageable);
    List<Music> findByArtistAndIsDeletedFalse(User artist);
    List<Music> findByAlbumAndIsDeletedFalse(Album album);
    Page<Music> findByArtistAndIsDeletedFalse(User artist, Pageable pageable);

    @Query("SELECT m FROM Music m WHERE m.isDeleted = false AND " +
            "(LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(m.artist.username) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Music> searchMusic(@Param("query") String query, Pageable pageable);

    List<Music> findByGenreAndIsDeletedFalse(Genre genre, Pageable pageable);
    long countByArtistAndIsDeletedFalse(User artist);

    @Query("SELECT DISTINCT m.genre FROM Music m WHERE m.artist = :artist AND m.isDeleted = false")
    List<Genre> findDistinctGenresByArtist(@Param("artist") User artist);
}