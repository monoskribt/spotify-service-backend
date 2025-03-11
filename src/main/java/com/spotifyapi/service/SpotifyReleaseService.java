package com.spotifyapi.service;

import com.spotifyapi.model.SpotifyRelease;

import java.util.Set;

public interface SpotifyReleaseService {

    void save(Set<SpotifyRelease> releaseList);

    void checkReleasesForAllUsers();
}
