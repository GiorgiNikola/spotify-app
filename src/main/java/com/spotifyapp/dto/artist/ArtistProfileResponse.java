package com.spotifyapp.dto.artist;

import com.spotifyapp.dto.music.MusicSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistProfileResponse {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private Long albumCount;
    private Long songCount;
    private List<String> genres;
    private List<MusicSummary> topSongs;
    private List<SimilarArtistResponse> similarArtists;
}
