package com.spotifyapi.config;

import com.spotifyapi.props.SpotifyProps;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class ProjectConfig {

    private final SpotifyProps spotifyProps;

    @Bean
    public SpotifyApi spotifyApi() {
        return new SpotifyApi.Builder()
                .setClientId(spotifyProps.clientId())
                .setClientSecret(spotifyProps.clientSecret())
                .setRedirectUri(URI.create(spotifyProps.redirectUrl()))
                .setHttpManager(new SpotifyHttpManager.Builder().setConnectionManager(
                        new PoolingHttpClientConnectionManager()
                ).build())
                .build();
    }
}
