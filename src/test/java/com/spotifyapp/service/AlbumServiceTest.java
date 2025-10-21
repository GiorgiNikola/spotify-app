package com.spotifyapp.service;

import com.spotifyapp.dto.album.AlbumRequest;
import com.spotifyapp.dto.album.AlbumResponse;
import com.spotifyapp.exception.UnauthorizedException;
import com.spotifyapp.model.entity.Album;
import com.spotifyapp.model.entity.User;
import com.spotifyapp.model.enums.UserRole;
import com.spotifyapp.repository.AlbumRepository;
import com.spotifyapp.repository.MusicRepository;
import com.spotifyapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private MusicRepository musicRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AlbumService albumService;

    private User artist;
    private User listener;
    private Album album;
    private AlbumRequest albumRequest;

    @BeforeEach
    void setUp() {
        artist = User.builder()
                .id(1L)
                .username("artist")
                .role(UserRole.ARTIST)
                .build();

        listener = User.builder()
                .id(2L)
                .username("listener")
                .role(UserRole.LISTENER)
                .build();

        album = Album.builder()
                .id(1L)
                .title("Test Album")
                .artist(artist)
                .releaseDate(LocalDate.now())
                .isDeleted(false)
                .build();

        albumRequest = new AlbumRequest();
        albumRequest.setTitle("New Album");
        albumRequest.setReleaseDate(LocalDate.now());
    }

    @Test
    void testCreateAlbum_Success() {
        when(userRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(artist));
        when(albumRepository.save(any(Album.class))).thenReturn(album);

        AlbumResponse response = albumService.createAlbum(albumRequest, 1L);

        assertNotNull(response);
        verify(albumRepository, times(1)).save(any(Album.class));
    }

    @Test
    void testCreateAlbum_NonArtistUser() {
        when(userRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(listener));

        assertThrows(UnauthorizedException.class, () ->
                albumService.createAlbum(albumRequest, 2L));
    }

    @Test
    void testGetAlbumById_Success() {
        when(albumRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(album));
        when(musicRepository.findByAlbumAndIsDeletedFalse(any())).thenReturn(Collections.emptyList());

        AlbumResponse response = albumService.getAlbumById(1L);

        assertNotNull(response);
        assertEquals("Test Album", response.getTitle());
    }

    @Test
    void testUpdateAlbum_Success() {
        when(albumRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(album));
        when(albumRepository.save(any(Album.class))).thenReturn(album);

        AlbumResponse response = albumService.updateAlbum(1L, albumRequest, 1L);

        assertNotNull(response);
        verify(albumRepository, times(1)).save(any(Album.class));
    }

    @Test
    void testUpdateAlbum_Unauthorized() {
        when(albumRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(album));

        assertThrows(UnauthorizedException.class, () ->
                albumService.updateAlbum(1L, albumRequest, 999L));
    }

    @Test
    void testDeleteAlbum_Success() {
        when(albumRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(album));
        when(albumRepository.save(any(Album.class))).thenReturn(album);

        albumService.deleteAlbum(1L, 1L);

        verify(albumRepository, times(1)).save(any(Album.class));
        assertTrue(album.getIsDeleted());
    }
}