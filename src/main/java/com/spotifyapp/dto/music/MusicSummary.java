package com.spotifyapp.dto.music;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicSummary {
    private Long id;
    private String title;
    private String genre;
    private Integer durationSeconds;
}
