package com.spotifyapi.repository;

import com.spotifyapi.model.SpotifyRelease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReleaseRepository extends JpaRepository<SpotifyRelease, String> {

    List<SpotifyRelease> findByUserId(String id);
}
