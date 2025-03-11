package com.spotifyapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class SpotifyApiException extends RuntimeException {

    public SpotifyApiException(String message) {
        super(message);
    }
}
