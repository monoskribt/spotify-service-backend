package controller;

import com.spotifyapi.SpotifyApiApplication;
import com.spotifyapi.controller.ProjectController;
import com.spotifyapi.dto.TokensDTO;
import com.spotifyapi.dto.UserInfoDTO;
import com.spotifyapi.props.CorsConfigurationProps;
import com.spotifyapi.service.SpotifyAuth;
import com.spotifyapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@ContextConfiguration(classes = SpotifyApiApplication.class)
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    SpotifyAuth spotifyAuth;

    @MockitoBean
    UserService userService;

    @MockitoBean
    CorsConfigurationProps corsProps;

    private String spotifyAuthUrl;
    private String authorizeCode;
    private String accessToken;
    private String refreshToken;
    private String redirectUrl;

    @BeforeEach
    void setUp() {
        spotifyAuthUrl = "https://spotify.com/auth";
        authorizeCode = "code";
        accessToken = "access_token";
        refreshToken = "refresh_token";
        redirectUrl = "http://allowed-origin?access_token=" + accessToken +
                "&refresh_token=" + refreshToken;
    }

    @Test
    void loginTest() throws Exception {
        when(spotifyAuth.authorize()).thenReturn(spotifyAuthUrl);

        mockMvc.perform(get("/api/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(spotifyAuthUrl));

        verify(spotifyAuth).authorize();
    }

    @Test
    void getProfileWithSaveUserTest() throws Exception {
        TokensDTO tokensDTO = new TokensDTO(accessToken, refreshToken);

        when(spotifyAuth.getAuthorizationTokens(authorizeCode))
                .thenReturn(tokensDTO);
        when(userService.isAlreadyExist()).thenReturn(false);
        when(corsProps.allowedOrigins()).thenReturn("http://allowed-origin");

        mockMvc.perform(get("/api/profile")
                        .param("code", authorizeCode))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", redirectUrl));

        verify(userService).saveUserOfData(eq(tokensDTO), any(UserInfoDTO.class));
        verify(userService, never()).updateUserData(tokensDTO);
    }

    @Test
    void getProfileWithUpdateUserTest() throws Exception {
        TokensDTO tokensDTO = new TokensDTO(accessToken, refreshToken);

        when(spotifyAuth.getAuthorizationTokens(authorizeCode))
                .thenReturn(tokensDTO);
        when(userService.isAlreadyExist()).thenReturn(true);
        when(corsProps.allowedOrigins()).thenReturn("http://allowed-origin");

        mockMvc.perform(get("/api/profile")
                        .param("code", authorizeCode))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", redirectUrl));

        verify(userService).updateUserData(tokensDTO);
        verify(userService, never()).saveUserOfData(eq(tokensDTO), any(UserInfoDTO.class));
    }
}
