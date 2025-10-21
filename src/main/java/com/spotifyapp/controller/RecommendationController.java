package com.spotifyapp.controller;

import com.spotifyapp.dto.artist.ArtistProfileResponse;
import com.spotifyapp.dto.playlist.PlaylistResponse;
import com.spotifyapp.security.UserDetailsImpl;
import com.spotifyapp.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "Music and artist recommendation endpoints")
@SecurityRequirement(name = "bearerAuth")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(
            summary = "Get artist profile",
            description = "Get artist profile with top songs and similar artists based on genre"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Artist profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ArtistProfileResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Artist not found / User is not an artist"
            )
    })
    @GetMapping("/artists/{artistId}")
    public ResponseEntity<ArtistProfileResponse> getArtistProfile(
            @Parameter(description = "Artist ID", example = "1") @PathVariable Long artistId) {
        ArtistProfileResponse profile = recommendationService.getArtistProfile(artistId);
        return ResponseEntity.ok(profile);
    }

    @Operation(
            summary = "Generate recommended playlists",
            description = "Generate personalized playlists based on user's listening history (top 3 genres). " +
                    "Deletes old system-generated playlists and creates new ones."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Playlists generated successfully",
                    content = @Content(schema = @Schema(implementation = PlaylistResponse.class))
            )
    })
    @PostMapping("/generate-playlists")
    public ResponseEntity<List<PlaylistResponse>> generateRecommendedPlaylists(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<PlaylistResponse> playlists = recommendationService.generateRecommendedPlaylists(
                userDetails.getUser().getId()
        );
        return ResponseEntity.ok(playlists);
    }
}