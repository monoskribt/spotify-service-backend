package com.spotifyapi.exception.global;

import com.spotifyapi.exception.SpotifyAuthException;
import com.spotifyapi.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalException {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleBadRequestException(Exception exception) {
        log.error("Unhandled exception occurred: {}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Something went wrong: " + exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(UserNotFoundException exception) {
        log.warn("User not found: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Something went wrong: " + exception.getMessage());
    }

    @ExceptionHandler(SpotifyAuthException.class)
    public ResponseEntity<String> handleUnauthorizedException(SpotifyAuthException exception) {
        log.warn("Authentication failed: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid or failed token: " + exception.getMessage());
    }
}
