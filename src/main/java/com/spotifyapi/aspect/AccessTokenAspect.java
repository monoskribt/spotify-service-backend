package com.spotifyapi.aspect;

import com.spotifyapi.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.SpotifyApi;

@Component
@Aspect
@RequiredArgsConstructor
public class AccessTokenAspect {

    private final SpotifyApi spotifyApi;
    private final TokenService tokenService;

    @Before("execution(* com.spotifyapi.service.impl.SpotifyServiceImpl.*(..)) && args(authorizationHeader,..)")
    public void setAccessToken(String authorizationHeader) {
            String accessToken = tokenService.extractAccessToken(authorizationHeader);
            spotifyApi.setAccessToken(accessToken);
    }
}

