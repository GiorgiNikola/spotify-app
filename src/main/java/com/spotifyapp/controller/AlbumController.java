package com.spotifyapp.controller;

import com.spotifyapp.dto.ApiResponse;
import com.spotifyapp.dto.album.AlbumRequest;
import com.spotifyapp.dto.album.AlbumResponse;
import com.spotifyapp.security.UserDetailsImpl;
import com.spotifyapp.service.AlbumService;
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
@RequestMapping("/api/albums")
@RequiredArgsConstructor
@Tag(name = "Albums", description = "Album management endpoints - create, update, and manage albums")
@SecurityRequirement(name = "bearerAuth")
public class AlbumController {

    private final AlbumService albumService;

    @Operation(
            summary = "Create new album",
            description = "Create a new album. Only ARTIST role can create albums."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Album created successfully",
                    content = @Content(
                            schema = @Schema(implementation = AlbumResponse.class),
                            examples = @ExampleObject(value = """
                                {
                                  "id": 1,
                                  "title": "Greatest Hits",
                                  "artistName": "john_artist",
                                  "artistId": 1,
                                  "coverImageUrl": "https://storage.example.com/covers/album1.jpg",
                                  "releaseDate": "2024-01-15"
                                }
                                """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Only artists can create albums",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"Only artists can create albums\"}")
                    )
            )
    })
    @PostMapping
    public ResponseEntity<AlbumResponse> createAlbum(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Album details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AlbumRequest.class),
                            examples = @ExampleObject(value = """
                                {
                                  "title": "Greatest Hits",
                                  "coverImageUrl": "https://storage.example.com/covers/album1.jpg",
                                  "releaseDate": "2024-01-15"
                                }
                                """)
                    )
            )
            @Valid @RequestBody AlbumRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        AlbumResponse album = albumService.createAlbum(request, userDetails.getUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(album);
    }

    @Operation(
            summary = "Get album by ID",
            description = "Get album details including all songs in the album"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Album found",
                    content = @Content(schema = @Schema(implementation = AlbumResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Album not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<AlbumResponse> getAlbumById(
            @Parameter(description = "Album ID", example = "1") @PathVariable Long id) {
        AlbumResponse album = albumService.getAlbumById(id);
        return ResponseEntity.ok(album);
    }

    @Operation(
            summary = "Get all albums",
            description = "Get paginated list of all albums"
    )
    @GetMapping
    public ResponseEntity<Page<AlbumResponse>> getAllAlbums(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AlbumResponse> albums = albumService.getAllAlbums(pageable);
        return ResponseEntity.ok(albums);
    }

    @Operation(
            summary = "Get albums by artist",
            description = "Get all albums by specific artist"
    )
    @GetMapping("/artist/{artistId}")
    public ResponseEntity<Page<AlbumResponse>> getAlbumsByArtist(
            @Parameter(description = "Artist ID", example = "1") @PathVariable Long artistId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AlbumResponse> albums = albumService.getAlbumsByArtist(artistId, pageable);
        return ResponseEntity.ok(albums);
    }

    @Operation(
            summary = "Update album",
            description = "Update album details. Only the artist who created the album can update it."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Album updated successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "You can only update your own albums"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<AlbumResponse> updateAlbum(
            @Parameter(description = "Album ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody AlbumRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        AlbumResponse album = albumService.updateAlbum(id, request, userDetails.getUser().getId());
        return ResponseEntity.ok(album);
    }

    @Operation(
            summary = "Delete album",
            description = "Soft delete album. Only the artist who created the album can delete it."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Album deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "You can only delete your own albums"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteAlbum(
            @Parameter(description = "Album ID", example = "1") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        albumService.deleteAlbum(id, userDetails.getUser().getId());
        return ResponseEntity.ok(new ApiResponse("Album deleted successfully"));
    }
}
