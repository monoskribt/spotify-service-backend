package com.spotifyapi.mapper;


import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

import java.util.Map;

public abstract class AbstractTrack {

    public abstract String getId();
    public abstract String getName();
    public abstract Map<String, String> getExternalUrls();
    public abstract ArtistSimplified[] getArtists();
}
