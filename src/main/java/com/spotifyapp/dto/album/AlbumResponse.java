package com.spotifyapp.dto.album;

import com.spotifyapp.dto.music.MusicSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumResponse {
    private Long id;
    private String title;
    private String artistName;
    private Long artistId;
    private String coverImageUrl;
    private LocalDate releaseDate;
    private List<MusicSummary> songs;
}
