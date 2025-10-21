package com.spotifyapp.repository;

import com.spotifyapp.model.entity.Music;
import com.spotifyapp.model.entity.WeeklyStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface WeeklyStatisticsRepository extends JpaRepository<WeeklyStatistics, Long> {
    Optional<WeeklyStatistics> findByMusicAndWeekStartDate(Music music, LocalDate weekStartDate);
}
