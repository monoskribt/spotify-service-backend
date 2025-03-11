package com.spotifyapi.controller;

import com.spotifyapi.dto.TokensDTO;
import com.spotifyapi.dto.UserInfoDTO;
import com.spotifyapi.props.CorsConfigurationProps;
import com.spotifyapi.service.SpotifyAuth;
import com.spotifyapi.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ProjectController {

    private final SpotifyAuth spotifyAuth;
    private final UserService userService;
    private final CorsConfigurationProps corsProps;

    @GetMapping("/login")
    public ResponseEntity<String> spotifyLogin() {
        return ResponseEntity.ok(spotifyAuth.authorize());
    }

    @GetMapping("/profile")
    public void getProfile(@RequestParam String code, HttpServletResponse response) throws IOException {
        TokensDTO tokens = spotifyAuth.getAuthorizationTokens(code);
        UserInfoDTO userInfoDTO = new UserInfoDTO();

        String redirectUrl = corsProps.allowedOrigins()
                + "?access_token=" + tokens.getAccessToken()
                + "&refresh_token=" + tokens.getRefreshToken();

        if(!userService.isAlreadyExist()) {
            userService.saveUserOfData(tokens, userInfoDTO);
        }
        else {
            userService.updateUserData(tokens);
        }
        response.sendRedirect(redirectUrl);
    }
}
