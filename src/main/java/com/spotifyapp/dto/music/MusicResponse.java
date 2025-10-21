package com.spotifyapp.dto.music;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicResponse {
    private Long id;
    private String title;
    private String artistName;
    private Long artistId;
    private String genre;
    private String albumTitle;
    private Integer durationSeconds;
    private String fileUrl;
}
