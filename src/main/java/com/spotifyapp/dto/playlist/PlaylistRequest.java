package com.spotifyapp.dto.playlist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PlaylistRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    private String description;
}
