package com.spotifyapi.mapper;

import lombok.RequiredArgsConstructor;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.util.Map;

@RequiredArgsConstructor
public class TrackSimplifiedWrapper extends AbstractTrack {

    private final TrackSimplified trackSimplified;

    @Override
    public String getId() {
        return trackSimplified.getId();
    }

    @Override
    public String getName() {
        return trackSimplified.getName();
    }

    @Override
    public Map<String, String> getExternalUrls() {
        return trackSimplified.getExternalUrls().getExternalUrls();
    }

    @Override
    public ArtistSimplified[] getArtists() {
        return trackSimplified.getArtists();
    }
}
