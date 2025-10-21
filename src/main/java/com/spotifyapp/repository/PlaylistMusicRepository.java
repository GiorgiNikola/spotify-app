package com.spotifyapp.repository;

import com.spotifyapp.model.entity.Music;
import com.spotifyapp.model.entity.Playlist;
import com.spotifyapp.model.entity.PlaylistMusic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistMusicRepository extends JpaRepository<PlaylistMusic, Long> {
    List<PlaylistMusic> findByPlaylistOrderByPositionAsc(Playlist playlist);
    Optional<PlaylistMusic> findByPlaylistAndMusic(Playlist playlist, Music music);
    boolean existsByPlaylistAndMusic(Playlist playlist, Music music);
    long countByPlaylist(Playlist playlist);

    @Query("SELECT MAX(pm.position) FROM PlaylistMusic pm WHERE pm.playlist = :playlist")
    Optional<Integer> findMaxPositionByPlaylist(@Param("playlist") Playlist playlist);

    void deleteByPlaylistAndMusic(Playlist playlist, Music music);
}