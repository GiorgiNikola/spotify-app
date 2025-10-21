package com.spotifyapp.dto.music;

import com.spotifyapp.model.enums.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MusicRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    private Long albumId;

    @NotNull(message = "Genre is required")
    private Genre genre;

    private Integer durationSeconds;

    @NotBlank(message = "File URL is required")
    private String fileUrl;
}
