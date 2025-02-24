package com.spotifyapi.service.impl;

import com.spotifyapi.dto.TokensDTO;
import com.spotifyapi.model.User;
import com.spotifyapi.service.SpotifyAuth;
import com.spotifyapi.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static com.spotifyapi.constant.ConstantExpireTokenTime.ONE_HOUR;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {

    private final SpotifyAuth spotifyAuth;

    @Override
    public String extractAccessToken(String authorizationHeader) {
        return Optional.ofNullable(authorizationHeader)
                .filter(token -> token.startsWith("access_token "))
                .map(header -> header.substring(13))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Authorization header"));
    }


    @Override
    public void getNewAccessToken(User u) {
        if(isValidRefreshToken(u)) {
            TokensDTO tokensDTO = spotifyAuth.getNewAccessToken(u.getRefreshToken());
            u.setAccessToken(tokensDTO.getAccessToken());
            u.setExpiresAccessTokenAt(Instant.now().plusSeconds(ONE_HOUR));
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }
    }


    @Override
    public boolean isValidAccessToken(User u) {
        if (u.getAccessToken().isEmpty() || u.getExpiresAccessTokenAt() == null) {
            return false;
        }
        return Instant.now().isBefore(u.getExpiresAccessTokenAt());
    }


    private boolean isValidRefreshToken(User u) {
        if(u.getRefreshToken() == null || u.getExpiresRefreshTokenAt() == null) {
            return false;
        }
        return Instant.now().isBefore(u.getExpiresRefreshTokenAt());
    }
}
