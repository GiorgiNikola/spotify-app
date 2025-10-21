package com.spotifyapp.dto.playlist;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddSongToPlaylistRequest {
    @NotNull(message = "Music ID is required")
    private Long musicId;
}
