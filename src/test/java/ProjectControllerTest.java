import com.spotifyapi.controller.ProjectController;
import com.spotifyapi.dto.TokensDTO;
import com.spotifyapi.dto.UserInfoDTO;
import com.spotifyapi.props.CorsConfigurationProps;
import com.spotifyapi.service.SpotifyAuth;
import com.spotifyapi.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ProjectControllerTest {

    @Mock
    private SpotifyAuth spotifyAuth;

    @Mock
    private UserService userService;

    @Mock
    private CorsConfigurationProps corsProps;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private ProjectController projectController;

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
    void testLogin() {
        when(spotifyAuth.authorize()).thenReturn(spotifyAuthUrl);

        ResponseEntity<String> result = projectController.spotifyLogin();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotEquals(result.getStatusCode(), HttpStatus.UNAUTHORIZED);
        assertEquals(spotifyAuthUrl, result.getBody());
        verify(spotifyAuth).authorize();
    }

    @Test
    void testGetProfileWithSaveUser() throws IOException {
        TokensDTO tokensDTO = new TokensDTO(accessToken, refreshToken);

        when(spotifyAuth.getAuthorizationTokens(authorizeCode))
                .thenReturn(tokensDTO);

        when(userService.isAlreadyExist()).thenReturn(false);
        when(corsProps.allowedOrigins()).thenReturn("http://allowed-origin");

        projectController.getProfile(authorizeCode, response);

        verify(userService).saveUserOfData(eq(tokensDTO), any(UserInfoDTO.class));
        verify(response).sendRedirect(redirectUrl);
    }

    @Test
    void testGetProfileWithUpdateUser() throws IOException {
        TokensDTO tokensDTO = new TokensDTO(accessToken, refreshToken);

        when(spotifyAuth.getAuthorizationTokens(authorizeCode))
                .thenReturn(tokensDTO);
        when(userService.isAlreadyExist()).thenReturn(true);
        when(corsProps.allowedOrigins()).thenReturn("http://allowed-origin");

        projectController.getProfile(authorizeCode, response);

        verify(userService).updateUserData(tokensDTO);
        verify(response).sendRedirect(redirectUrl);
    }
}
