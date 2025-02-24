package com.spotifyapi.mapper;

import lombok.RequiredArgsConstructor;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.Map;

@RequiredArgsConstructor
public class TrackWrapper extends AbstractTrack {

    private final Track track;

    @Override
    public String getId() {
        return track.getId();
    }

    @Override
    public String getName() {
        return track.getName();
    }

    @Override
    public Map<String, String> getExternalUrls() {
        return track.getExternalUrls().getExternalUrls();
    }

    @Override
    public ArtistSimplified[] getArtists() {
        return track.getArtists();
    }
}
