package com.spotifyapi.service;

import com.spotifyapi.model.User;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

public interface SpotifyPlaylistService {

    boolean isAlreadyExistById(String playlistId);

    void savePlaylistToDatabase(PlaylistSimplified playlist, User user);
}
