package com.spotifyapi.service.impl;

import com.spotifyapi.service.SendInfoReleasesToTelegram;
import com.spotifyapi.service.SpotifyReleaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendInfoReleasesToTelegramImpl implements SendInfoReleasesToTelegram {

    private final SpotifyReleaseService spotifyReleaseService;

    @Scheduled(cron = "0 0 8 * * *")
    @Override
    public void sendToTelegram() {
        spotifyReleaseService.checkReleasesForAllUsers();
    }
}
