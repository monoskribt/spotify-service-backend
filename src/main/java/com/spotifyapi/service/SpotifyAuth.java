package com.spotifyapi.service;


import com.spotifyapi.dto.TokensDTO;

public interface SpotifyAuth {

    String authorize();

    TokensDTO getAuthorizationTokens(String code);

    TokensDTO getNewAccessToken(String refreshToken);
}
