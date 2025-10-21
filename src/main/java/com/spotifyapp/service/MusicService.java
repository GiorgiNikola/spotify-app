package com.spotifyapp.service;

import com.spotifyapp.dto.music.MusicRequest;
import com.spotifyapp.dto.music.MusicResponse;
import com.spotifyapp.exception.ResourceNotFoundException;
import com.spotifyapp.exception.UnauthorizedException;
import com.spotifyapp.model.entity.Album;
import com.spotifyapp.model.entity.ListeningHistory;
import com.spotifyapp.model.entity.Music;
import com.spotifyapp.model.entity.User;
import com.spotifyapp.model.enums.UserRole;
import com.spotifyapp.repository.AlbumRepository;
import com.spotifyapp.repository.ListeningHistoryRepository;
import com.spotifyapp.repository.MusicRepository;
import com.spotifyapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MusicService {

    private final MusicRepository musicRepository;
    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;
    private final ListeningHistoryRepository listeningHistoryRepository;

    @Transactional
    public MusicResponse createMusic(MusicRequest request, Long artistId) {
        User artist = userRepository.findByIdAndIsDeletedFalse(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found"));

        if (artist.getRole() != UserRole.ARTIST) {
            throw new UnauthorizedException("Only artists can upload music");
        }

        Album album = null;
        if (request.getAlbumId() != null) {
            album = albumRepository.findByIdAndIsDeletedFalse(request.getAlbumId())
                    .orElseThrow(() -> new ResourceNotFoundException("Album not found"));

            if (!album.getArtist().getId().equals(artistId)) {
                throw new UnauthorizedException("You can only add music to your own albums");
            }
        }

        Music music = Music.builder()
                .title(request.getTitle())
                .artist(artist)
                .album(album)
                .genre(request.getGenre())
                .durationSeconds(request.getDurationSeconds())
                .fileUrl(request.getFileUrl())
                .build();

        music = musicRepository.save(music);
        return mapToResponse(music);
    }

    @Transactional
    public MusicResponse getMusicById(Long id, Long userId) {
        Music music = musicRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Music not found"));

        // Record listening history
        if (userId != null) {
            User user = userRepository.findByIdAndIsDeletedFalse(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            ListeningHistory history = ListeningHistory.builder()
                    .user(user)
                    .music(music)
                    .build();

            listeningHistoryRepository.save(history);
        }

        return mapToResponse(music);
    }

    @Transactional(readOnly = true)
    public Page<MusicResponse> searchMusic(String query, Pageable pageable) {
        return musicRepository.searchMusic(query, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<MusicResponse> getAllMusic(Pageable pageable) {
        return musicRepository.findByIsDeletedFalse(pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public MusicResponse updateMusic(Long id, MusicRequest request, Long userId) {
        Music music = musicRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Music not found"));

        if (!music.getArtist().getId().equals(userId)) {
            throw new UnauthorizedException("You can only update your own music");
        }

        if (request.getAlbumId() != null) {
            Album album = albumRepository.findByIdAndIsDeletedFalse(request.getAlbumId())
                    .orElseThrow(() -> new ResourceNotFoundException("Album not found"));

            if (!album.getArtist().getId().equals(userId)) {
                throw new UnauthorizedException("You can only add music to your own albums");
            }
            music.setAlbum(album);
        }

        music.setTitle(request.getTitle());
        music.setGenre(request.getGenre());
        music.setDurationSeconds(request.getDurationSeconds());
        music.setFileUrl(request.getFileUrl());

        music = musicRepository.save(music);
        return mapToResponse(music);
    }

    @Transactional
    public void deleteMusic(Long id, Long userId) {
        Music music = musicRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Music not found"));

        if (!music.getArtist().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own music");
        }

        music.setIsDeleted(true);
        musicRepository.save(music);
    }

    @Transactional
    public void deleteMusicByAdmin(Long id) {
        Music music = musicRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Music not found"));
        music.setIsDeleted(true);
        musicRepository.save(music);
    }

    private MusicResponse mapToResponse(Music music) {
        return MusicResponse.builder()
                .id(music.getId())
                .title(music.getTitle())
                .artistName(music.getArtist().getUsername())
                .artistId(music.getArtist().getId())
                .genre(music.getGenre().name())
                .albumTitle(music.getAlbum() != null ? music.getAlbum().getTitle() : null)
                .durationSeconds(music.getDurationSeconds())
                .fileUrl(music.getFileUrl())
                .build();
    }
}
