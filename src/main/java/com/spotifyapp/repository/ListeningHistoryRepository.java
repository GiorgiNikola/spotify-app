package com.spotifyapp.repository;

import com.spotifyapp.model.entity.ListeningHistory;
import com.spotifyapp.model.entity.Music;
import com.spotifyapp.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ListeningHistoryRepository extends JpaRepository<ListeningHistory, Long> {
    List<ListeningHistory> findByUserAndListenedAtAfter(User user, LocalDateTime after);

    @Query("SELECT m.genre, COUNT(lh) as cnt FROM ListeningHistory lh " +
            "JOIN lh.music m WHERE lh.user = :user AND lh.listenedAt > :after " +
            "GROUP BY m.genre ORDER BY cnt DESC")
    List<Object[]> findTopGenresByUser(@Param("user") User user, @Param("after") LocalDateTime after);

    @Query("SELECT COUNT(DISTINCT lh.user) FROM ListeningHistory lh " +
            "WHERE lh.music = :music AND lh.listenedAt BETWEEN :start AND :end")
    Long countUniqueListenersByMusicAndPeriod(
            @Param("music") Music music,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(lh) FROM ListeningHistory lh " +
            "WHERE lh.music = :music AND lh.listenedAt BETWEEN :start AND :end")
    Long countListensByMusicAndPeriod(
            @Param("music") Music music,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
