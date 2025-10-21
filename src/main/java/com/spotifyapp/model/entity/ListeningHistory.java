package com.spotifyapp.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "listening_history", indexes = {
        @Index(name = "idx_lh_user", columnList = "user_id"),
        @Index(name = "idx_lh_music", columnList = "music_id"),
        @Index(name = "idx_lh_listened_at", columnList = "listened_at"),
        @Index(name = "idx_lh_user_time", columnList = "user_id, listened_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListeningHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "music_id", nullable = false)
    private Music music;

    @CreationTimestamp
    @Column(name = "listened_at", nullable = false, updatable = false)
    private LocalDateTime listenedAt;
}