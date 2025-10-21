package com.spotifyapp.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "weekly_statistics",
        indexes = {
                @Index(name = "idx_ws_music", columnList = "music_id"),
                @Index(name = "idx_ws_week_start", columnList = "week_start_date")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"music_id", "week_start_date"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "music_id", nullable = false)
    private Music music;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "week_end_date", nullable = false)
    private LocalDate weekEndDate;

    @Column(name = "listen_count", nullable = false)
    @Builder.Default
    private Long listenCount = 0L;

    @Column(name = "unique_listeners", nullable = false)
    @Builder.Default
    private Long uniqueListeners = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
