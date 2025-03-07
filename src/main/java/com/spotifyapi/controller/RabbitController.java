package com.spotifyapi.controller;

import com.spotifyapi.service.SpotifyReleaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rabbit")
@RequiredArgsConstructor
public class RabbitController {

    private final SpotifyReleaseService spotifyReleaseService;

    @GetMapping
    public void getReleases() {
        spotifyReleaseService.checkReleasesForAllUsers();
    }
}
