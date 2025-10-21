package com.spotifyapp.controller;

import com.spotifyapp.dto.ApiResponse;
import com.spotifyapp.dto.playlist.PlaylistRequest;
import com.spotifyapp.dto.playlist.PlaylistResponse;
import com.spotifyapp.security.UserDetailsImpl;
import com.spotifyapp.service.PlaylistService;
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
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
@Tag(name = "Playlists", description = "Playlist management endpoints - create, manage playlists and add/remove songs")
@SecurityRequirement(name = "bearerAuth")
public class PlaylistController {

    private final PlaylistService playlistService;

    @Operation(
            summary = "Create new playlist",
            description = "Create a new personal playlist"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Playlist created successfully",
                    content = @Content(schema = @Schema(implementation = PlaylistResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<PlaylistResponse> createPlaylist(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Playlist details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PlaylistRequest.class),
                            examples = @ExampleObject(value = """
                                {
                                  "name": "My Favorite Songs",
                                  "description": "Collection of my all-time favorite tracks"
                                }
                                """)
                    )
            )
            @Valid @RequestBody PlaylistRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        PlaylistResponse playlist = playlistService.createPlaylist(request, userDetails.getUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(playlist);
    }

    @Operation(
            summary = "Get my playlists",
            description = "Get all playlists created by current user (including system-generated)"
    )
    @GetMapping("/my")
    public ResponseEntity<Page<PlaylistResponse>> getMyPlaylists(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PlaylistResponse> playlists = playlistService.getMyPlaylists(userDetails.getUser().getId(), pageable);
        return ResponseEntity.ok(playlists);
    }

    @Operation(
            summary = "Get playlist by ID",
            description = "Get playlist details including all songs"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Playlist found",
                    content = @Content(schema = @Schema(implementation = PlaylistResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Playlist not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<PlaylistResponse> getPlaylistById(
            @Parameter(description = "Playlist ID", example = "1") @PathVariable Long id) {
        PlaylistResponse playlist = playlistService.getPlaylistById(id);
        return ResponseEntity.ok(playlist);
    }

    @Operation(
            summary = "Update playlist",
            description = "Update playlist details. Cannot modify system-generated playlists."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Playlist updated successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "You can only update your own playlists / Cannot modify system-generated playlists"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<PlaylistResponse> updatePlaylist(
            @Parameter(description = "Playlist ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody PlaylistRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        PlaylistResponse playlist = playlistService.updatePlaylist(id, request, userDetails.getUser().getId());
        return ResponseEntity.ok(playlist);
    }

    @Operation(
            summary = "Delete playlist",
            description = "Delete playlist. Cannot delete system-generated playlists."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Playlist deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "You can only delete your own playlists / Cannot delete system-generated playlists"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deletePlaylist(
            @Parameter(description = "Playlist ID", example = "1") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        playlistService.deletePlaylist(id, userDetails.getUser().getId());
        return ResponseEntity.ok(new ApiResponse("Playlist deleted successfully"));
    }

    @Operation(
            summary = "Add song to playlist",
            description = "Add a song to playlist. Cannot modify system-generated playlists."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Song added to playlist successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Song already in playlist"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "You can only modify your own playlists / Cannot modify system-generated playlists"
            )
    })
    @PostMapping("/{playlistId}/songs/{musicId}")
    public ResponseEntity<ApiResponse> addSongToPlaylist(
            @Parameter(description = "Playlist ID", example = "1") @PathVariable Long playlistId,
            @Parameter(description = "Music ID", example = "5") @PathVariable Long musicId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        playlistService.addSongToPlaylist(playlistId, musicId, userDetails.getUser().getId());
        return ResponseEntity.ok(new ApiResponse("Song added to playlist successfully"));
    }

    @Operation(
            summary = "Remove song from playlist",
            description = "Remove a song from playlist. Cannot modify system-generated playlists."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Song removed from playlist successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "You can only modify your own playlists / Cannot modify system-generated playlists"
            )
    })
    @DeleteMapping("/{playlistId}/songs/{musicId}")
    public ResponseEntity<ApiResponse> removeSongFromPlaylist(
            @Parameter(description = "Playlist ID", example = "1") @PathVariable Long playlistId,
            @Parameter(description = "Music ID", example = "5") @PathVariable Long musicId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        playlistService.removeSongFromPlaylist(playlistId, musicId, userDetails.getUser().getId());
        return ResponseEntity.ok(new ApiResponse("Song removed from playlist successfully"));
    }
}