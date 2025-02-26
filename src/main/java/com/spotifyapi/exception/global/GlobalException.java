package com.spotifyapi.exception.global;

import com.spotifyapi.dto.ErrorResponseDTO;
import com.spotifyapi.exception.SpotifyApiException;
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

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadRequestException(RuntimeException exception) {
        log.error("Error during Runtime: {}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(HttpStatus.BAD_REQUEST.value(),
                        "Unexpected error",
                        exception.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFoundException(UserNotFoundException exception) {
        log.warn("User not found: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO(HttpStatus.NOT_FOUND.value(),
                        "User not found",
                        exception.getMessage()));
    }

    @ExceptionHandler(SpotifyAuthException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnauthorizedException(SpotifyAuthException exception) {
        log.warn("Authentication failed: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDTO(HttpStatus.UNAUTHORIZED.value(),
                        "Authentication failed",
                        exception.getMessage()));
    }

    @ExceptionHandler(SpotifyApiException.class)
    public ResponseEntity<ErrorResponseDTO> handleSpotifyApiException(SpotifyApiException exception) {
        log.warn("Error during executing method with participation of spotify api library: {}",
                exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponseDTO(HttpStatus.BAD_GATEWAY.value(),
                        "Spotify API error", exception.getMessage()));
    }
}
