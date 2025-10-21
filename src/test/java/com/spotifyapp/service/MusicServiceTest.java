package com.spotifyapp.service;

import com.spotifyapp.dto.music.MusicRequest;
import com.spotifyapp.dto.music.MusicResponse;
import com.spotifyapp.exception.UnauthorizedException;
import com.spotifyapp.model.entity.Music;
import com.spotifyapp.model.entity.User;
import com.spotifyapp.model.enums.Genre;
import com.spotifyapp.model.enums.UserRole;
import com.spotifyapp.repository.AlbumRepository;
import com.spotifyapp.repository.ListeningHistoryRepository;
import com.spotifyapp.repository.MusicRepository;
import com.spotifyapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MusicServiceTest {

    @Mock
    private MusicRepository musicRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ListeningHistoryRepository listeningHistoryRepository;

    @InjectMocks
    private MusicService musicService;

    private User artist;
    private User listener;
    private Music music;
    private MusicRequest musicRequest;

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

        music = Music.builder()
                .id(1L)
                .title("Test Song")
                .artist(artist)
                .genre(Genre.ROCK)
                .durationSeconds(240)
                .fileUrl("http://example.com/song.mp3")
                .isDeleted(false)
                .build();

        musicRequest = new MusicRequest();
        musicRequest.setTitle("New Song");
        musicRequest.setGenre(Genre.POP);
        musicRequest.setDurationSeconds(200);
        musicRequest.setFileUrl("http://example.com/new.mp3");
    }

    @Test
    void testCreateMusic_Success() {
        when(userRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(artist));
        when(musicRepository.save(any(Music.class))).thenReturn(music);

        MusicResponse response = musicService.createMusic(musicRequest, 1L);

        assertNotNull(response);
        verify(musicRepository, times(1)).save(any(Music.class));
    }

    @Test
    void testCreateMusic_NonArtistUser() {
        when(userRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(listener));

        assertThrows(UnauthorizedException.class, () ->
                musicService.createMusic(musicRequest, 2L));
    }

    @Test
    void testSearchMusic_ByTitle() {
        Page<Music> musicPage = new PageImpl<>(Arrays.asList(music));
        when(musicRepository.searchMusic(anyString(), any())).thenReturn(musicPage);

        Page<MusicResponse> results = musicService.searchMusic("Test", PageRequest.of(0, 20));

        assertNotNull(results);
        assertEquals(1, results.getTotalElements());
    }

    @Test
    void testGetMusicById_RecordsListeningHistory() {
        when(musicRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(music));
        when(userRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(listener));
        when(listeningHistoryRepository.save(any())).thenReturn(null);

        MusicResponse response = musicService.getMusicById(1L, 2L);

        assertNotNull(response);
        verify(listeningHistoryRepository, times(1)).save(any());
    }

    @Test
    void testUpdateMusic_Unauthorized() {
        when(musicRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(music));

        assertThrows(UnauthorizedException.class, () ->
                musicService.updateMusic(1L, musicRequest, 999L));
    }

    @Test
    void testDeleteMusic_Success() {
        when(musicRepository.findByIdAndIsDeletedFalse(anyLong())).thenReturn(Optional.of(music));
        when(musicRepository.save(any(Music.class))).thenReturn(music);

        musicService.deleteMusic(1L, 1L);

        verify(musicRepository, times(1)).save(any(Music.class));
    }
}
