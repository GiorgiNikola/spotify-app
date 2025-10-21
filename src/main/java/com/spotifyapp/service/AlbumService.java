package com.spotifyapp.service;

import com.spotifyapp.dto.album.AlbumRequest;
import com.spotifyapp.dto.album.AlbumResponse;
import com.spotifyapp.dto.music.MusicSummary;
import com.spotifyapp.exception.ResourceNotFoundException;
import com.spotifyapp.exception.UnauthorizedException;
import com.spotifyapp.model.entity.Album;
import com.spotifyapp.model.entity.Music;
import com.spotifyapp.model.entity.User;
import com.spotifyapp.model.enums.UserRole;
import com.spotifyapp.repository.AlbumRepository;
import com.spotifyapp.repository.MusicRepository;
import com.spotifyapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final MusicRepository musicRepository;
    private final UserRepository userRepository;

    @Transactional
    public AlbumResponse createAlbum(AlbumRequest request, Long artistId) {
        User artist = userRepository.findByIdAndIsDeletedFalse(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found"));

        if (artist.getRole() != UserRole.ARTIST) {
            throw new UnauthorizedException("Only artists can create albums");
        }

        Album album = Album.builder()
                .title(request.getTitle())
                .artist(artist)
                .coverImageUrl(request.getCoverImageUrl())
                .releaseDate(request.getReleaseDate())
                .build();

        album = albumRepository.save(album);
        return mapToResponse(album);
    }

    @Transactional(readOnly = true)
    public AlbumResponse getAlbumById(Long id) {
        Album album = albumRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found"));
        return mapToResponseWithSongs(album);
    }

    @Transactional(readOnly = true)
    public Page<AlbumResponse> getAllAlbums(Pageable pageable) {
        return albumRepository.findByIsDeletedFalse(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AlbumResponse> getAlbumsByArtist(Long artistId, Pageable pageable) {
        User artist = userRepository.findByIdAndIsDeletedFalse(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found"));
        return albumRepository.findByArtistAndIsDeletedFalse(artist, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public AlbumResponse updateAlbum(Long id, AlbumRequest request, Long userId) {
        Album album = albumRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found"));

        if (!album.getArtist().getId().equals(userId)) {
            throw new UnauthorizedException("You can only update your own albums");
        }

        album.setTitle(request.getTitle());
        album.setCoverImageUrl(request.getCoverImageUrl());
        album.setReleaseDate(request.getReleaseDate());

        album = albumRepository.save(album);
        return mapToResponse(album);
    }

    @Transactional
    public void deleteAlbum(Long id, Long userId) {
        Album album = albumRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found"));

        if (!album.getArtist().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own albums");
        }

        album.setIsDeleted(true);
        albumRepository.save(album);
    }

    @Transactional
    public void deleteAlbumByAdmin(Long id) {
        Album album = albumRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found"));
        album.setIsDeleted(true);
        albumRepository.save(album);
    }

    private AlbumResponse mapToResponse(Album album) {
        return AlbumResponse.builder()
                .id(album.getId())
                .title(album.getTitle())
                .artistName(album.getArtist().getUsername())
                .artistId(album.getArtist().getId())
                .coverImageUrl(album.getCoverImageUrl())
                .releaseDate(album.getReleaseDate())
                .build();
    }

    private AlbumResponse mapToResponseWithSongs(Album album) {
        List<Music> songs = musicRepository.findByAlbumAndIsDeletedFalse(album);

        List<MusicSummary> songSummaries = songs.stream()
                .map(music -> MusicSummary.builder()
                        .id(music.getId())
                        .title(music.getTitle())
                        .genre(music.getGenre().name())
                        .durationSeconds(music.getDurationSeconds())
                        .build())
                .collect(Collectors.toList());

        return AlbumResponse.builder()
                .id(album.getId())
                .title(album.getTitle())
                .artistName(album.getArtist().getUsername())
                .artistId(album.getArtist().getId())
                .coverImageUrl(album.getCoverImageUrl())
                .releaseDate(album.getReleaseDate())
                .songs(songSummaries)
                .build();
    }
}

