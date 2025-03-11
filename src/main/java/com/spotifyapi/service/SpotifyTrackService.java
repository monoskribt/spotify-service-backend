package com.spotifyapi.service;

import com.spotifyapi.mapper.AbstractTrack;
import com.spotifyapi.model.SpotifyTrackFromPlaylist;
import com.spotifyapi.model.SpotifyUserPlaylist;

import java.util.Set;

public interface SpotifyTrackService {

    Set<String> getExistingTrackIds(String playlistId);

    SpotifyTrackFromPlaylist convertTrackToTrackDBEntity(
            AbstractTrack track, SpotifyUserPlaylist playlist);
}
