package com.spotifyapp.service;

import com.spotifyapp.model.entity.Music;
import com.spotifyapp.model.entity.User;
import com.spotifyapp.model.entity.WeeklyStatistics;
import com.spotifyapp.model.enums.Genre;
import com.spotifyapp.model.enums.UserRole;
import com.spotifyapp.repository.ListeningHistoryRepository;
import com.spotifyapp.repository.MusicRepository;
import com.spotifyapp.repository.WeeklyStatisticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private MusicRepository musicRepository;

    @Mock
    private ListeningHistoryRepository listeningHistoryRepository;

    @Mock
    private WeeklyStatisticsRepository weeklyStatisticsRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    private Music music;
    private WeeklyStatistics statistics;

    @BeforeEach
    void setUp() {
        User artist = User.builder()
                .id(1L)
                .username("artist")
                .role(UserRole.ARTIST)
                .build();

        music = Music.builder()
                .id(1L)
                .title("Test Song")
                .artist(artist)
                .genre(Genre.ROCK)
                .isDeleted(false)
                .build();

        statistics = WeeklyStatistics.builder()
                .id(1L)
                .music(music)
                .weekStartDate(LocalDate.now())
                .weekEndDate(LocalDate.now())
                .listenCount(100L)
                .uniqueListeners(50L)
                .build();
    }

    @Test
    void testGenerateWeeklyStatistics_Success() {
        when(musicRepository.findAll()).thenReturn(Arrays.asList(music));
        when(listeningHistoryRepository.countListensByMusicAndPeriod(any(), any(), any()))
                .thenReturn(100L);
        when(listeningHistoryRepository.countUniqueListenersByMusicAndPeriod(any(), any(), any()))
                .thenReturn(50L);
        when(weeklyStatisticsRepository.findByMusicAndWeekStartDate(any(), any()))
                .thenReturn(Optional.empty());
        when(weeklyStatisticsRepository.save(any(WeeklyStatistics.class)))
                .thenReturn(statistics);

        statisticsService.generateWeeklyStatistics();

        verify(weeklyStatisticsRepository, times(1)).save(any(WeeklyStatistics.class));
    }

    @Test
    void testGenerateWeeklyStatistics_UpdateExisting() {
        when(musicRepository.findAll()).thenReturn(Arrays.asList(music));
        when(listeningHistoryRepository.countListensByMusicAndPeriod(any(), any(), any()))
                .thenReturn(150L);
        when(listeningHistoryRepository.countUniqueListenersByMusicAndPeriod(any(), any(), any()))
                .thenReturn(75L);
        when(weeklyStatisticsRepository.findByMusicAndWeekStartDate(any(), any()))
                .thenReturn(Optional.of(statistics));
        when(weeklyStatisticsRepository.save(any(WeeklyStatistics.class)))
                .thenReturn(statistics);

        statisticsService.generateWeeklyStatistics();

        verify(weeklyStatisticsRepository, times(1)).save(any(WeeklyStatistics.class));
    }

    @Test
    void testGenerateWeeklyStatistics_SkipDeletedMusic() {
        music.setIsDeleted(true);
        when(musicRepository.findAll()).thenReturn(Arrays.asList(music));

        statisticsService.generateWeeklyStatistics();

        verify(weeklyStatisticsRepository, never()).save(any());
    }
}
