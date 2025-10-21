package com.spotifyapp.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "playlist_music",
        indexes = {
                @Index(name = "idx_pm_playlist", columnList = "playlist_id"),
                @Index(name = "idx_pm_music", columnList = "music_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"playlist_id", "music_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaylistMusic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "music_id", nullable = false)
    private Music music;

    @Column(nullable = false)
    private Integer position;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;
}
