import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotifyapi.SpotifyApiApplication;
import com.spotifyapi.controller.SpotifyController;
import com.spotifyapi.dto.spotify_entity.SpotifyArtistDTO;
import com.spotifyapi.dto.spotify_entity.SpotifyPlaylistsDTO;
import com.spotifyapi.dto.spotify_entity.SpotifyReleaseDTO;
import com.spotifyapi.service.SpotifyService;
import org.apache.catalina.connector.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(SpotifyController.class)
@ContextConfiguration(classes = SpotifyApiApplication.class)
class SpotifyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    SpotifyService spotifyService;

    private String accessToken;
    private Long releaseOfDay;

    @BeforeEach
    void init() {
        accessToken = "test-token";
        releaseOfDay = 30L;
    }

    @Test
    void testGetMyArtist() throws Exception {
        List<SpotifyArtistDTO> artists = List.of(
                new SpotifyArtistDTO("Artist-1"),
                new SpotifyArtistDTO("Artist-2")
        );

        when(spotifyService.getFollowedArtist(accessToken, SpotifyArtistDTO.class))
                .thenReturn(artists);

        mockMvc.perform(get("/api/spotify/artists")
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Artist-1"))
                .andExpect(jsonPath("$[1].name").value("Artist-2"))
                .andDo(res -> System.out.println(res.getResponse().getContentAsString()));
    }

    @Test
    void getReleases() throws Exception {
        List<SpotifyReleaseDTO> listOfReleases = List.of(
                new SpotifyReleaseDTO("1", "Release-1"),
                new SpotifyReleaseDTO("2", "Release-2"),
                new SpotifyReleaseDTO("3", "Release-3")
        );

        when(spotifyService
                .getReleases(accessToken, releaseOfDay, SpotifyReleaseDTO.class))
                .thenReturn(listOfReleases);

        mockMvc.perform(get("/api/spotify/releases")
                        .header("Authorization", accessToken)
                        .param("releaseOfDay", releaseOfDay.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Release-1"))
                .andExpect(jsonPath("$[1].name").value("Release-2"))
                .andExpect(jsonPath("$[2].name").value("Release-3"));
    }

    @Test
    void getMyPlaylists() throws Exception {
        Set<SpotifyPlaylistsDTO> playlists = new HashSet<>();
        playlists.add(new SpotifyPlaylistsDTO("1", "Playlist-1"));
        playlists.add(new SpotifyPlaylistsDTO("2", "Playlist-2"));
        playlists.add(new SpotifyPlaylistsDTO("3", "Playlist-3"));

        when(spotifyService.getOfUsersPlaylists(accessToken))
                .thenReturn(playlists);

        MvcResult result = mockMvc.perform(get("/api/spotify/playlists")
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        Set<SpotifyPlaylistsDTO> responsePlaylists = mapper.readValue(responseBody,
                new TypeReference<>() {
                });

        assertFalse(responsePlaylists.isEmpty());
        assertEquals(3, playlists.size());
        assertTrue(responsePlaylists.stream()
                .anyMatch(ply -> ply.getName().equals("Playlist-2")));
    }

    @Test
    void saveReleasesWithOKStatus() throws Exception {
        String playlistId = "playlist-1";

        when(spotifyService.saveReleasesToPlaylistById(accessToken, playlistId, releaseOfDay))
                .thenReturn(Response.SC_OK);

        mockMvc.perform(post("/api/spotify/playlists/{playlistId}/releases", playlistId)
                        .param("releaseOfDay", releaseOfDay.toString())
                        .header("Authorization", accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void saveReleasesWithNoContentStatus() throws Exception {
        String playlistId = "playlist-1";

        when(spotifyService.saveReleasesToPlaylistById(accessToken, playlistId, releaseOfDay))
                .thenReturn(Response.SC_NO_CONTENT);

        mockMvc.perform(post("/api/spotify/playlists/{playlistId}/releases", playlistId)
                        .param("releaseOfDay", releaseOfDay.toString())
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(204));
    }

    @Test
    void deleteAllItemsFromPlaylistWithOKStatus() throws Exception {
        String playlistId = "playlist-1";

        when(spotifyService.deleteAllOfTracksFromPlaylistById(accessToken, playlistId))
                .thenReturn(Response.SC_OK);

        mockMvc.perform(delete("/api/spotify/playlists/{playlistId}/items", playlistId)
                        .header("Authorization", accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void deleteAllItemsFromPlaylistWithNoContentStatus() throws Exception {
        String playlistId = "playlist-1";

        when(spotifyService.deleteAllOfTracksFromPlaylistById(accessToken, playlistId))
                .thenReturn(Response.SC_NO_CONTENT);

        mockMvc.perform(delete("/api/spotify/playlists/{playlistId}/items", playlistId)
                    .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(204));
    }
}


