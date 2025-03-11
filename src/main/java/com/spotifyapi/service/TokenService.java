package com.spotifyapi.service;


import com.spotifyapi.model.User;

public interface TokenService {

    String extractAccessToken(String authorizationHeader);

    void getNewAccessToken(User u);

    boolean isValidAccessToken(User u);
}
