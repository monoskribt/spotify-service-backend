package com.spotifyapi.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "spotify_release")
public class SpotifyRelease {

    @Id
    private String id;

    private String name;

    private LocalDate localDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
