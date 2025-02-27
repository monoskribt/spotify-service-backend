package com.spotifyapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class ProgressArtistsUpdate {

    private int processed;
    private int total;
}
