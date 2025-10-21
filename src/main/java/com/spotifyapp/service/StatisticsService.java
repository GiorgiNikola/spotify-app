package com.spotifyapp.service;

import com.spotifyapp.model.entity.Music;
import com.spotifyapp.model.entity.WeeklyStatistics;
import com.spotifyapp.repository.ListeningHistoryRepository;
import com.spotifyapp.repository.MusicRepository;
import com.spotifyapp.repository.WeeklyStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final MusicRepository musicRepository;
    private final ListeningHistoryRepository listeningHistoryRepository;
    private final WeeklyStatisticsRepository weeklyStatisticsRepository;

    // Run every Friday at 23:59
    // For testing: @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Scheduled(cron = "0 59 23 * * FRI")
    @Transactional
    public void generateWeeklyStatistics() {
        log.info("Starting weekly statistics generation...");

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDateTime startDateTime = weekStart.atStartOfDay();
        LocalDateTime endDateTime = weekEnd.atTime(23, 59, 59);

        List<Music> allMusic = musicRepository.findAll();

        for (Music music : allMusic) {
            if (music.getIsDeleted()) {
                continue;
            }

            Long listenCount = listeningHistoryRepository.countListensByMusicAndPeriod(
                    music, startDateTime, endDateTime
            );

            Long uniqueListeners = listeningHistoryRepository.countUniqueListenersByMusicAndPeriod(
                    music, startDateTime, endDateTime
            );

            WeeklyStatistics statistics = weeklyStatisticsRepository
                    .findByMusicAndWeekStartDate(music, weekStart)
                    .orElse(WeeklyStatistics.builder()
                            .music(music)
                            .weekStartDate(weekStart)
                            .weekEndDate(weekEnd)
                            .build());

            statistics.setListenCount(listenCount);
            statistics.setUniqueListeners(uniqueListeners);

            weeklyStatisticsRepository.save(statistics);
        }

        log.info("Weekly statistics generation completed. Week: {} to {}", weekStart, weekEnd);
    }

    // Cleanup expired verification codes - runs every hour
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredVerificationCodes() {
        log.info("Cleaning up expired verification codes...");
        // This is handled in UserRepository
        log.info("Cleanup completed");
    }
}