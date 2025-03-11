package com.spotifyapi.service;

import com.spotifyapi.model.SpotifyRelease;
import com.spotifyapi.model.User;

import java.util.Set;

public interface RabbitMQService {

    void sendMessageToTelegram(User user, Set<SpotifyRelease> releases);
}
