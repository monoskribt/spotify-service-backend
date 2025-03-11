package com.spotifyapi.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spotify")
public record SpotifyProps(String clientId, String clientSecret, String redirectUrl, String userInfoUri) {
}
