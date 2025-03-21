package com.spotifyapi.service;

import com.spotifyapi.mapper.AbstractTrack;
import com.spotifyapi.model.SpotifyTrackFromPlaylist;
import com.spotifyapi.model.SpotifyUserPlaylist;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.util.Set;

public interface SpotifyTrackService {

    Set<String> getExistingTrackIdsFromDb(String playlistId);

    Paging<PlaylistTrack> getTracksFromSpotifyPlaylistById(SpotifyUserPlaylist spotifyUserPlaylist);

    SpotifyTrackFromPlaylist convertTrackToTrackDBEntity(
            AbstractTrack track, SpotifyUserPlaylist playlist);
}
