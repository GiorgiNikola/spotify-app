package com.spotifyapp.dto.playlist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistSongResponse {
    private Long id;
    private String title;
    private String artistName;
    private String genre;
    private Integer position;
    private Integer durationSeconds;
}
