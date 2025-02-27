package com.spotifyapi.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "spotify_artist")
@AllArgsConstructor
@NoArgsConstructor
public class SpotifyArtist {

    @Id
    private String id;
    private String name;
}
