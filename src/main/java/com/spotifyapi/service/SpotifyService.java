package com.spotifyapi.service;

import com.spotifyapi.dto.spotify_entity.SpotifyPlaylistsDTO;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;

import java.util.List;
import java.util.Set;

public interface SpotifyService {

    <T> List<T> getFollowedArtist(String authorizationHeader, Class<T> returnTypeOfClass);

    Set<SpotifyPlaylistsDTO> getOfUsersPlaylists(String authorizationHeader);

    List<AlbumSimplified> getReleases(String authorizationHeader);

    <T> List<T> getReleases(String authorizationHeader, Long releaseOfDay, Class<T> returnTypeOfClass);

    int saveReleasesToPlaylistById(String authorizationHeader, String playlistId, Long releaseOfDay);

    int deleteAllOfTracksFromPlaylistById(String authorizationHeader, String playlistId);
}