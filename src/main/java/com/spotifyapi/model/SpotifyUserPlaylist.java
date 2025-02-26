package com.spotifyapi.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "spotify_user_playlist")
public class SpotifyUserPlaylist {

    @Id
    private String id;

    private String name;
    private String externalUrl;
    private String ownerName;
    private String snapshotId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "userPlaylist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpotifyTrackFromPlaylist> tracks;
}
