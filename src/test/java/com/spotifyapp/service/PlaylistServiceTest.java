package com.spotifyapp.service;

import com.spotifyapp.dto.playlist.PlaylistRequest;
import com.spotifyapp.dto.playlist.PlaylistResponse;
import com.spotifyapp.exception.BadRequestException;
import com.spotifyapp.exception.UnauthorizedException;
import com.spotifyapp.model.entity.Music;
import com.spotifyapp.model.entity.Playlist;
import com.spotifyapp.model.entity.PlaylistMusic;
import com.spotifyapp.model.entity.User;
import com.spotifyapp.model.enums.Genre;
import com.spotifyapp.model.enums.UserRole;
import com.spotifyapp.repository.MusicRepository;
import com.spotifyapp.repository.PlaylistMusicRepository;
import com.spotifyapp.repository.PlaylistRepository;
import com.spotifyapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private PlaylistMusicRepository playlistMusicRepository;

    @Mock
    private MusicRepository musicRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PlaylistService playlistService;

    private User user;
    private Playlist playlist;
    private Music music;
    private PlaylistRequest playlistRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .role(UserRole.LISTENER)
                .build();

        playlist = Playlist.builder()
                .id(1L)
                .name("My Playlist")
                .owner(user)
                .isSystemGenerated(false)
                .isDeleted(false)
                .build();

        music = Music.builder()
                .id(1L)
                .title("Test Song")
                .genre(Genre.ROCK)
                .isDeleted(false)
                .build();

        playlistRequest = new PlaylistRequest();
        playlistRequest.setName("New Playlist");
        playlistRequest.setDescription("Description");
    }

    @Test
    void testCreatePlaylist_Success() {
        when(userRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(user));
        when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);
        when(playlistMusicRepository.countByPlaylist(any())).thenReturn(0L);

        PlaylistResponse response = playlistService.createPlaylist(playlistRequest, 1L);

        assertNotNull(response);
        verify(playlistRepository, times(1)).save(any(Playlist.class));
    }

    @Test
    void testAddSongToPlaylist_Success() {
        when(playlistRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(playlist));
        when(musicRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(music));
        when(playlistMusicRepository.existsByPlaylistAndMusic(any(), any())).thenReturn(false);
        when(playlistMusicRepository.findMaxPositionByPlaylist(any())).thenReturn(Optional.of(5));
        when(playlistMusicRepository.save(any(PlaylistMusic.class))).thenReturn(null);

        playlistService.addSongToPlaylist(1L, 1L, 1L);

        verify(playlistMusicRepository, times(1)).save(any(PlaylistMusic.class));
    }

    @Test
    void testAddSongToPlaylist_AlreadyExists() {
        when(playlistRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(playlist));
        when(musicRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(music));
        when(playlistMusicRepository.existsByPlaylistAndMusic(any(), any())).thenReturn(true);

        assertThrows(BadRequestException.class, () ->
                playlistService.addSongToPlaylist(1L, 1L, 1L));
    }

    @Test
    void testUpdatePlaylist_Unauthorized() {
        when(playlistRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(playlist));

        assertThrows(UnauthorizedException.class, () ->
                playlistService.updatePlaylist(1L, playlistRequest, 999L));
    }

    @Test
    void testUpdatePlaylist_SystemGenerated() {
        playlist.setIsSystemGenerated(true);
        when(playlistRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(playlist));

        assertThrows(UnauthorizedException.class, () ->
                playlistService.updatePlaylist(1L, playlistRequest, 1L));
    }

    @Test
    void testDeletePlaylist_Success() {
        when(playlistRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(playlist));
        when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);

        playlistService.deletePlaylist(1L, 1L);

        verify(playlistRepository, times(1)).save(any(Playlist.class));
        assertTrue(playlist.getIsDeleted());
    }
}
