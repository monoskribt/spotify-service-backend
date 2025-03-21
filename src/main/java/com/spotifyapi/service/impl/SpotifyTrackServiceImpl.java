package com.spotifyapi.service.impl;

import com.spotifyapi.exception.SpotifyApiException;
import com.spotifyapi.mapper.AbstractTrack;
import com.spotifyapi.model.SpotifyTrackFromPlaylist;
import com.spotifyapi.model.SpotifyUserPlaylist;
import com.spotifyapi.repository.TrackRepository;
import com.spotifyapi.service.SpotifyTrackService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class SpotifyTrackServiceImpl implements SpotifyTrackService {

    private final TrackRepository trackRepository;
    private final SpotifyApi spotifyApi;

    @Override
    public Set<String> getExistingTrackIdsFromDb(String playlistId) {
        return trackRepository.findAllByUserPlaylistId(playlistId)
                .stream()
                .map(SpotifyTrackFromPlaylist::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public Paging<PlaylistTrack> getTracksFromSpotifyPlaylistById(SpotifyUserPlaylist spotifyUserPlaylist) {
        Paging<PlaylistTrack> tracksFromSpotifyPlaylist;
        try {
            tracksFromSpotifyPlaylist = spotifyApi.getPlaylistsItems(spotifyUserPlaylist.getId())
                    .build()
                    .execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.warn("Error getting of playlist items: {}", e.getMessage());
            throw new SpotifyApiException("Error getting of playlist items: " + e.getMessage());
        }
        return tracksFromSpotifyPlaylist;
    }

    @Override
    public SpotifyTrackFromPlaylist convertTrackToTrackDBEntity(
            AbstractTrack track, SpotifyUserPlaylist playlist) {

        SpotifyTrackFromPlaylist trackEntity = new SpotifyTrackFromPlaylist();

        trackEntity.setId(track.getId());
        trackEntity.setName(track.getName());
        trackEntity.setExternalUrl(getSpotifyUrl(track.getExternalUrls()));
        trackEntity.setArtistName(getArtistName(track.getArtists()));
        trackEntity.setUserPlaylist(playlist);

        return trackEntity;
    }

    private String getSpotifyUrl(Map<String, String> externalUrls) {
        return externalUrls != null ? externalUrls.get("spotify") : null;
    }

    private String getArtistName(ArtistSimplified[] artists) {
        return (artists != null && artists.length > 0) ? artists[0].getName() : null;
    }

}
