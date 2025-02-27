package com.spotifyapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class SpotifyAuthException extends RuntimeException {

    public SpotifyAuthException(String message) {
        super(message);
    }
}
