package com.spotifyapp.service;

import com.spotifyapp.dto.artist.ArtistProfileResponse;
import com.spotifyapp.dto.artist.SimilarArtistResponse;
import com.spotifyapp.dto.music.MusicSummary;
import com.spotifyapp.dto.playlist.PlaylistResponse;
import com.spotifyapp.exception.ResourceNotFoundException;
import com.spotifyapp.model.entity.Music;
import com.spotifyapp.model.entity.Playlist;
import com.spotifyapp.model.entity.PlaylistMusic;
import com.spotifyapp.model.entity.User;
import com.spotifyapp.model.enums.Genre;
import com.spotifyapp.model.enums.UserRole;
import com.spotifyapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserRepository userRepository;
    private final MusicRepository musicRepository;
    private final AlbumRepository albumRepository;
    private final ListeningHistoryRepository listeningHistoryRepository;
    private final PlaylistRepository playlistRepository;
    private final PlaylistMusicRepository playlistMusicRepository;

    @Transactional(readOnly = true)
    public ArtistProfileResponse getArtistProfile(Long artistId) {
        User artist = userRepository.findByIdAndIsDeletedFalse(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found"));

        if (artist.getRole() != UserRole.ARTIST) {
            throw new ResourceNotFoundException("User is not an artist");
        }

        // Get artist genres
        List<Genre> artistGenres = musicRepository.findDistinctGenresByArtist(artist);

        // Get top songs (simplified - by recent listens)
        List<Music> artistMusic = musicRepository.findByArtistAndIsDeletedFalse(artist);
        List<MusicSummary> topSongs = artistMusic.stream()
                .limit(10)
                .map(music -> MusicSummary.builder()
                        .id(music.getId())
                        .title(music.getTitle())
                        .genre(music.getGenre().name())
                        .durationSeconds(music.getDurationSeconds())
                        .build())
                .collect(Collectors.toList());

        // Get similar artists
        List<SimilarArtistResponse> similarArtists = getSimilarArtists(artist, artistGenres);

        return ArtistProfileResponse.builder()
                .id(artist.getId())
                .username(artist.getUsername())
                .firstName(artist.getFirstName())
                .lastName(artist.getLastName())
                .albumCount(albumRepository.countByArtistAndIsDeletedFalse(artist))
                .songCount(musicRepository.countByArtistAndIsDeletedFalse(artist))
                .genres(artistGenres.stream().map(Enum::name).collect(Collectors.toList()))
                .topSongs(topSongs)
                .similarArtists(similarArtists)
                .build();
    }

    private List<SimilarArtistResponse> getSimilarArtists(User currentArtist, List<Genre> currentGenres) {
        List<User> allArtists = userRepository.findByRoleAndIsDeletedFalse(UserRole.ARTIST);

        Map<User, List<Genre>> artistGenreMap = new HashMap<>();
        for (User artist : allArtists) {
            if (!artist.getId().equals(currentArtist.getId())) {
                List<Genre> genres = musicRepository.findDistinctGenresByArtist(artist);
                if (!genres.isEmpty()) {
                    artistGenreMap.put(artist, genres);
                }
            }
        }

        // Calculate similarity score
        List<SimilarArtistResponse> similarArtists = new ArrayList<>();
        for (Map.Entry<User, List<Genre>> entry : artistGenreMap.entrySet()) {
            List<Genre> sharedGenres = entry.getValue().stream()
                    .filter(currentGenres::contains)
                    .collect(Collectors.toList());

            if (!sharedGenres.isEmpty()) {
                similarArtists.add(SimilarArtistResponse.builder()
                        .id(entry.getKey().getId())
                        .username(entry.getKey().getUsername())
                        .sharedGenres(sharedGenres.stream().map(Enum::name).collect(Collectors.toList()))
                        .build());
            }
        }

        // Sort by number of shared genres
        similarArtists.sort((a, b) -> Integer.compare(b.getSharedGenres().size(), a.getSharedGenres().size()));

        return similarArtists.stream().limit(10).collect(Collectors.toList());
    }

    @Transactional
    public List<PlaylistResponse> generateRecommendedPlaylists(Long userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get user's listening history from last 3 months
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        List<Object[]> topGenres = listeningHistoryRepository.findTopGenresByUser(user, threeMonthsAgo);

        // Delete old system-generated playlists for this user
        List<Playlist> oldPlaylists = playlistRepository.findByOwnerAndIsSystemGeneratedTrueAndIsDeletedFalse(user);
        for (Playlist oldPlaylist : oldPlaylists) {
            oldPlaylist.setIsDeleted(true);
            playlistRepository.save(oldPlaylist);
        }

        List<PlaylistResponse> generatedPlaylists = new ArrayList<>();

        // Generate playlist for top 3 genres
        int count = Math.min(3, topGenres.size());
        for (int i = 0; i < count; i++) {
            Object[] genreData = topGenres.get(i);
            Genre genre = (Genre) genreData[0];

            // Create playlist
            Playlist playlist = Playlist.builder()
                    .name(genre.name() + " Mix for You")
                    .description("Based on your listening history")
                    .owner(user)
                    .isSystemGenerated(true)
                    .build();

            playlist = playlistRepository.save(playlist);

            // Add top songs of this genre
            List<Music> topSongs = musicRepository.findByGenreAndIsDeletedFalse(
                    genre,
                    PageRequest.of(0, 20)
            );

            int position = 1;
            for (Music music : topSongs) {
                PlaylistMusic playlistMusic = PlaylistMusic.builder()
                        .playlist(playlist)
                        .music(music)
                        .position(position++)
                        .build();
                playlistMusicRepository.save(playlistMusic);
            }

            PlaylistResponse response = PlaylistResponse.builder()
                    .id(playlist.getId())
                    .name(playlist.getName())
                    .description(playlist.getDescription())
                    .ownerId(user.getId())
                    .ownerUsername(user.getUsername())
                    .isSystemGenerated(true)
                    .songCount(topSongs.size())
                    .build();

            generatedPlaylists.add(response);
        }

        return generatedPlaylists;
    }
}
