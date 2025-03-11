package com.spotifyapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "spotify_track_from_playlist")
public class SpotifyTrackFromPlaylist {

    @Id
    private String id;

    private String name;
    private String externalUrl;
    private String artistName;

    @ManyToOne
    @JoinColumn(name = "playlist_id", nullable = false)
    private SpotifyUserPlaylist userPlaylist;
}
