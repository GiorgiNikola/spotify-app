package com.spotifyapp.service;

import com.spotifyapp.dto.artist.ArtistProfileResponse;
import com.spotifyapp.exception.ResourceNotFoundException;
import com.spotifyapp.model.entity.Playlist;
import com.spotifyapp.model.entity.User;
import com.spotifyapp.model.enums.Genre;
import com.spotifyapp.model.enums.UserRole;
import com.spotifyapp.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MusicRepository musicRepository;

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ListeningHistoryRepository listeningHistoryRepository;

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private PlaylistMusicRepository playlistMusicRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    private User artist;

    @BeforeEach
    void setUp() {
        artist = User.builder()
                .id(1L)
                .username("artist")
                .role(UserRole.ARTIST)
                .build();
    }

    @Test
    void testGetArtistProfile_Success() {
        when(userRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(artist));
        when(musicRepository.findDistinctGenresByArtist(any())).thenReturn(Arrays.asList(Genre.ROCK, Genre.POP));
        when(musicRepository.findByArtistAndIsDeletedFalse(any())).thenReturn(Collections.emptyList());
        when(albumRepository.countByArtistAndIsDeletedFalse(any())).thenReturn(5L);
        when(musicRepository.countByArtistAndIsDeletedFalse(any())).thenReturn(20L);
        when(userRepository.findByRoleAndIsDeletedFalse(any())).thenReturn(Collections.emptyList());

        ArtistProfileResponse response = recommendationService.getArtistProfile(1L);

        assertNotNull(response);
        assertEquals("artist", response.getUsername());
        assertEquals(5L, response.getAlbumCount());
        assertEquals(20L, response.getSongCount());
    }

    @Test
    void testGetArtistProfile_NotArtist() {
        User listener = User.builder()
                .id(2L)
                .username("listener")
                .role(UserRole.LISTENER)
                .build();

        when(userRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(listener));

        assertThrows(ResourceNotFoundException.class, () ->
                recommendationService.getArtistProfile(2L));
    }

    @Test
    void testGenerateRecommendedPlaylists_Success() {
        User user = User.builder()
                .id(1L)
                .username("user")
                .role(UserRole.LISTENER)
                .build();

        List<Object[]> topGenres = List.<Object[]>of(new Object[]{Genre.ROCK, 100L});

        Playlist savedPlaylist = Playlist.builder()
                .id(10L)
                .name("Recommended - ROCK")
                .isSystemGenerated(true)
                .build();

        when(userRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(user));
        when(listeningHistoryRepository.findTopGenresByUser(any(), any()))
                .thenReturn(topGenres);
        when(playlistRepository.findByOwnerAndIsSystemGeneratedTrueAndIsDeletedFalse(any()))
                .thenReturn(Collections.emptyList());
        when(playlistRepository.save(any())).thenReturn(savedPlaylist);
        when(musicRepository.findByGenreAndIsDeletedFalse(any(), any()))
                .thenReturn(Collections.emptyList());

        var playlists = recommendationService.generateRecommendedPlaylists(1L);

        assertNotNull(playlists);
        verify(playlistRepository, atLeastOnce()).save(any());
    }



}