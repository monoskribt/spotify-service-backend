package com.spotifyapi.service.impl;

import com.spotifyapi.customAnnotation.RetryAfterRequest;
import com.spotifyapi.exception.SpotifyApiException;
import com.spotifyapi.model.SpotifyArtist;
import com.spotifyapi.service.PaginationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.ModelObjectType;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaginationServiceImpl implements PaginationService {

    private final SpotifyApi spotifyApi;

    @Override
    public List<SpotifyArtist> paginationOfArtists() {
        List<SpotifyArtist> followedArtists = new ArrayList<>();

        int limit = 50;
        String cursor = "0";

        while (cursor != null) {
            PagingCursorbased<Artist> responseFollowerArtists;
            try {
                responseFollowerArtists = spotifyApi.getUsersFollowedArtists(ModelObjectType.ARTIST)
                        .limit(limit)
                        .after(cursor)
                        .build()
                        .execute();
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                log.warn("Error during getting of followed artist users: {}", e.getMessage());
                throw new SpotifyApiException("Error during getting of followed artist users: " + e.getMessage());
            }

            if(responseFollowerArtists != null) {
                followedArtists.addAll(Arrays.stream(responseFollowerArtists.getItems())
                        .map(artist -> new SpotifyArtist(artist.getId(), artist.getName()))
                        .toList());
                cursor = responseFollowerArtists.getCursors()[0].getAfter();
            } else {
                break;
            }
        }
        return followedArtists;
    }

    @RetryAfterRequest
    @Override
    public List<AlbumSimplified> paginationOfReleasesArtist(String artistId, Long releaseOfDay) {
        List<AlbumSimplified> albums = new ArrayList<>();
        DateTimeFormatter yearMonthDayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        int offset = 0;
        String nextPage;

        do {
            Paging<AlbumSimplified> items;
            try {
                items = spotifyApi.getArtistsAlbums(artistId)
                        .setQueryParameter("include_groups", "album,single")
                        .limit(50)
                        .offset(offset)
                        .build()
                        .execute();
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                log.warn("Error during getting of artist albums: {}", e.getMessage(), e.getCause());
                throw new SpotifyApiException("Error during getting of artist albums: " + e.getMessage());
            }


            if (items == null || items.getItems().length == 0) {
                log.info("No albums for artist: {}", artistId);
                break;
            }

            albums.addAll(Arrays.stream(items.getItems())
                    .filter(album -> {
                        try {
                            if (album.getReleaseDate() == null || album.getReleaseDate().isEmpty()) {
                                return false;
                            }
                            return checkAndParseDate(releaseOfDay, album, yearMonthDayFormatter);
                        } catch (DateTimeParseException e) {
                            log.warn("Error parsing release date for album: {}. Error: {}", album.getName(), e.getMessage());
                            return false;
                        }
                    })
                    .toList());

            nextPage = items.getNext();
            offset += 50;

        } while (nextPage != null);

        return albums;
    }

    private static boolean checkAndParseDate(Long releaseOfDay,
                                             AlbumSimplified album,
                                             DateTimeFormatter yearMonthDayFormatter) {
        LocalDate releaseDate;
        String releaseDateString = album.getReleaseDate();

        if (releaseDateString.length() == 4) {
            releaseDate = LocalDate.parse(releaseDateString + "-01-01", yearMonthDayFormatter);
        } else if (releaseDateString.length() == 7) {
            releaseDate = LocalDate.parse(releaseDateString + "-01", yearMonthDayFormatter);
        } else {
            releaseDate = LocalDate.parse(releaseDateString, yearMonthDayFormatter);
        }

        return releaseDate.isAfter(LocalDate.now().minusDays(releaseOfDay));
    }

    @RetryAfterRequest
    @Override
    public List<TrackSimplified> paginationOfSaveReleasesMethod(String albumId) {
        List<TrackSimplified> tracks = new ArrayList<>();

        int offset = 0;
        String nextPage;

        do {
            Paging<TrackSimplified> tracksPage;
            try {
                tracksPage = spotifyApi.getAlbumsTracks(albumId)
                        .limit(50)
                        .offset(offset)
                        .build()
                        .execute();
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                log.warn("Error during getting of track albums: {}", e.getMessage(), e.getCause());
                throw new SpotifyApiException("Error during getting of track albums: " + e.getMessage());
            }

            if(tracksPage.getItems() == null || tracksPage.getItems().length == 0) {
                break;
            }

            tracks.addAll(Arrays.asList(tracksPage.getItems()));

            nextPage = tracksPage.getNext();
            offset += 50;
        } while (nextPage != null);

        return tracks;
    }

    @Override
    public List<PlaylistTrack> paginationOfDeleteReleasesMethod(String playlistId) {
        List<PlaylistTrack> myTracks = new ArrayList<>();

        int offset = 0;
        boolean hasMore;

        do {
            PlaylistTrack[] trackInPlaylist;
            try {
                trackInPlaylist = spotifyApi.getPlaylistsItems(playlistId)
                        .offset(offset)
                        .limit(50)
                        .build()
                        .execute()
                        .getItems();
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                log.warn("Error during getting of playlist items: {}", e.getMessage(), e.getCause());
                throw new SpotifyApiException("Error during getting of playlist items: " + e.getMessage());
            }

            if (trackInPlaylist != null) {
                myTracks.addAll(Arrays.asList(trackInPlaylist));
            } else {
                log.info("Playlist is empty.");
                break;
            }

            hasMore = trackInPlaylist.length == 0;
            offset += 50;
        } while (!hasMore);

        return myTracks;
    }
}
