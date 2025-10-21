package com.spotifyapp.controller;

import com.spotifyapp.dto.ApiResponse;
import com.spotifyapp.dto.music.MusicRequest;
import com.spotifyapp.dto.music.MusicResponse;
import com.spotifyapp.security.UserDetailsImpl;
import com.spotifyapp.service.MusicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
@Tag(name = "Music", description = "Music management endpoints - upload, search, and manage music tracks")
@SecurityRequirement(name = "bearerAuth")
public class MusicController {

    private final MusicService musicService;

    @Operation(
            summary = "Upload new music",
            description = "Upload a new music track. Only ARTIST role can upload music. " +
                    "Optionally associate with an album."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Music uploaded successfully",
                    content = @Content(schema = @Schema(implementation = MusicResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Only artists can upload music"
            )
    })
    @PostMapping
    public ResponseEntity<MusicResponse> createMusic(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Music details",
                    content = @Content(
                            schema = @Schema(implementation = MusicRequest.class),
                            examples = @ExampleObject(value = """
                                {
                                  "title": "Bohemian Rhapsody",
                                  "albumId": 1,
                                  "genre": "ROCK",
                                  "durationSeconds": 354,
                                  "fileUrl": "https://storage.example.com/music/bohemian-rhapsody.mp3"
                                }
                                """)
                    )
            )
            @Valid @RequestBody MusicRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        MusicResponse music = musicService.createMusic(request, userDetails.getUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(music);
    }

    @Operation(
            summary = "Get music by ID",
            description = "Get music details by ID. Records listening history if user is authenticated."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Music found",
                    content = @Content(schema = @Schema(implementation = MusicResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Music not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<MusicResponse> getMusicById(
            @Parameter(description = "Music ID", example = "1") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails != null ? userDetails.getUser().getId() : null;
        MusicResponse music = musicService.getMusicById(id, userId);
        return ResponseEntity.ok(music);
    }

    @Operation(
            summary = "Search music",
            description = "Search music by title or artist name. Supports partial matching."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Search results",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    @GetMapping("/search")
    public ResponseEntity<Page<MusicResponse>> searchMusic(
            @Parameter(description = "Search query", example = "fra")
            @RequestParam String query,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MusicResponse> results = musicService.searchMusic(query, pageable);
        return ResponseEntity.ok(results);
    }

    @Operation(
            summary = "Get all music",
            description = "Get paginated list of all music tracks"
    )
    @GetMapping
    public ResponseEntity<Page<MusicResponse>> getAllMusic(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MusicResponse> music = musicService.getAllMusic(pageable);
        return ResponseEntity.ok(music);
    }

    @Operation(
            summary = "Update music",
            description = "Update music details. Only the artist who uploaded can update."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Music updated successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "You can only update your own music"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<MusicResponse> updateMusic(
            @Parameter(description = "Music ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody MusicRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        MusicResponse music = musicService.updateMusic(id, request, userDetails.getUser().getId());
        return ResponseEntity.ok(music);
    }

    @Operation(
            summary = "Delete music",
            description = "Soft delete music. Only the artist who uploaded can delete."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Music deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "You can only delete your own music"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteMusic(
            @Parameter(description = "Music ID", example = "1") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        musicService.deleteMusic(id, userDetails.getUser().getId());
        return ResponseEntity.ok(new ApiResponse("Music deleted successfully"));
    }
}