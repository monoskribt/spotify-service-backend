package com.spotifyapi.service.impl;

import com.spotifyapi.mapper.AbstractTrack;
import com.spotifyapi.model.SpotifyTrackFromPlaylist;
import com.spotifyapi.model.SpotifyUserPlaylist;
import com.spotifyapi.repository.TrackRepository;
import com.spotifyapi.service.SpotifyTrackService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class SpotifyTrackServiceImpl implements SpotifyTrackService {

    private final TrackRepository trackRepository;

    @Override
    public Set<String> getExistingTrackIds(String playlistId) {
        return trackRepository.findAllByUserPlaylistId(playlistId)
                .stream()
                .map(SpotifyTrackFromPlaylist::getId)
                .collect(Collectors.toSet());
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
