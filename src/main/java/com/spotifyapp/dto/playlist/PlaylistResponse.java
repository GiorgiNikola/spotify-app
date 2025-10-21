package com.spotifyapp.dto.playlist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistResponse {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private String ownerUsername;
    private Boolean isSystemGenerated;
    private Integer songCount;
    private List<PlaylistSongResponse> songs;
}
