package com.spotifyapp.service;

import com.spotifyapp.dto.playlist.PlaylistRequest;
import com.spotifyapp.dto.playlist.PlaylistResponse;
import com.spotifyapp.dto.playlist.PlaylistSongResponse;
import com.spotifyapp.exception.BadRequestException;
import com.spotifyapp.exception.ResourceNotFoundException;
import com.spotifyapp.exception.UnauthorizedException;
import com.spotifyapp.model.entity.Music;
import com.spotifyapp.model.entity.Playlist;
import com.spotifyapp.model.entity.PlaylistMusic;
import com.spotifyapp.model.entity.User;
import com.spotifyapp.repository.MusicRepository;
import com.spotifyapp.repository.PlaylistMusicRepository;
import com.spotifyapp.repository.PlaylistRepository;
import com.spotifyapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistMusicRepository playlistMusicRepository;
    private final MusicRepository musicRepository;
    private final UserRepository userRepository;

    @Transactional
    public PlaylistResponse createPlaylist(PlaylistRequest request, Long userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Playlist playlist = Playlist.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(user)
                .isSystemGenerated(false)
                .build();

        playlist = playlistRepository.save(playlist);
        return mapToResponse(playlist);
    }

    @Transactional(readOnly = true)
    public Page<PlaylistResponse> getMyPlaylists(Long userId, Pageable pageable) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return playlistRepository.findByOwnerAndIsDeletedFalse(user, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public PlaylistResponse getPlaylistById(Long id) {
        Playlist playlist = playlistRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));
        return mapToResponseWithSongs(playlist);
    }

    @Transactional
    public PlaylistResponse updatePlaylist(Long id, PlaylistRequest request, Long userId) {
        Playlist playlist = playlistRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

        if (!playlist.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("You can only update your own playlists");
        }

        if (playlist.getIsSystemGenerated()) {
            throw new UnauthorizedException("Cannot modify system-generated playlists");
        }

        playlist.setName(request.getName());
        playlist.setDescription(request.getDescription());

        playlist = playlistRepository.save(playlist);
        return mapToResponse(playlist);
    }

    @Transactional
    public void deletePlaylist(Long id, Long userId) {
        Playlist playlist = playlistRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

        if (!playlist.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own playlists");
        }

        if (playlist.getIsSystemGenerated()) {
            throw new UnauthorizedException("Cannot delete system-generated playlists");
        }

        playlist.setIsDeleted(true);
        playlistRepository.save(playlist);
    }

    @Transactional
    public void addSongToPlaylist(Long playlistId, Long musicId, Long userId) {
        Playlist playlist = playlistRepository.findByIdAndIsDeletedFalse(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

        if (!playlist.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("You can only modify your own playlists");
        }

        if (playlist.getIsSystemGenerated()) {
            throw new UnauthorizedException("Cannot modify system-generated playlists");
        }

        Music music = musicRepository.findByIdAndIsDeletedFalse(musicId)
                .orElseThrow(() -> new ResourceNotFoundException("Music not found"));

        if (playlistMusicRepository.existsByPlaylistAndMusic(playlist, music)) {
            throw new BadRequestException("Song already in playlist");
        }

        Integer maxPosition = playlistMusicRepository.findMaxPositionByPlaylist(playlist)
                .orElse(0);

        PlaylistMusic playlistMusic = PlaylistMusic.builder()
                .playlist(playlist)
                .music(music)
                .position(maxPosition + 1)
                .build();

        playlistMusicRepository.save(playlistMusic);
    }

    @Transactional
    public void removeSongFromPlaylist(Long playlistId, Long musicId, Long userId) {
        Playlist playlist = playlistRepository.findByIdAndIsDeletedFalse(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

        if (!playlist.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("You can only modify your own playlists");
        }

        if (playlist.getIsSystemGenerated()) {
            throw new UnauthorizedException("Cannot modify system-generated playlists");
        }

        Music music = musicRepository.findByIdAndIsDeletedFalse(musicId)
                .orElseThrow(() -> new ResourceNotFoundException("Music not found"));

        playlistMusicRepository.deleteByPlaylistAndMusic(playlist, music);
    }

    private PlaylistResponse mapToResponse(Playlist playlist) {
        long songCount = playlistMusicRepository.countByPlaylist(playlist);

        return PlaylistResponse.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .ownerId(playlist.getOwner().getId())
                .ownerUsername(playlist.getOwner().getUsername())
                .isSystemGenerated(playlist.getIsSystemGenerated())
                .songCount((int) songCount)
                .build();
    }

    private PlaylistResponse mapToResponseWithSongs(Playlist playlist) {
        List<PlaylistMusic> playlistMusics = playlistMusicRepository.findByPlaylistOrderByPositionAsc(playlist);

        List<PlaylistSongResponse> songs = playlistMusics.stream()
                .map(pm -> PlaylistSongResponse.builder()
                        .id(pm.getMusic().getId())
                        .title(pm.getMusic().getTitle())
                        .artistName(pm.getMusic().getArtist().getUsername())
                        .genre(pm.getMusic().getGenre().name())
                        .position(pm.getPosition())
                        .durationSeconds(pm.getMusic().getDurationSeconds())
                        .build())
                .collect(Collectors.toList());

        return PlaylistResponse.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .ownerId(playlist.getOwner().getId())
                .ownerUsername(playlist.getOwner().getUsername())
                .isSystemGenerated(playlist.getIsSystemGenerated())
                .songCount(songs.size())
                .songs(songs)
                .build();
    }
}
