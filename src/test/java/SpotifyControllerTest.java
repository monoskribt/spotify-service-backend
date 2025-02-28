import com.spotifyapi.controller.SpotifyController;
import com.spotifyapi.dto.spotify_entity.SpotifyArtistDTO;
import com.spotifyapi.dto.spotify_entity.SpotifyPlaylistsDTO;
import com.spotifyapi.dto.spotify_entity.SpotifyReleaseDTO;
import com.spotifyapi.service.SpotifyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;


import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SpotifyControllerTest {

    @Mock
    private SpotifyService spotifyService;

    @InjectMocks
    private SpotifyController spotifyController;

    private String authorizationHeader;
    private Long releaseOfDay;
    private String playlistId;

    @BeforeEach
    void setUp() {
        authorizationHeader = "test-token";
        releaseOfDay = 10L;
        playlistId = "12345";
    }

    @Test
    void testGetMyArtist() {
        List<SpotifyArtistDTO> listOfArtist = Arrays.asList(new SpotifyArtistDTO("Artist-1"),
                new SpotifyArtistDTO("Artist-2"));

        when(spotifyService.getFollowedArtist(eq(authorizationHeader), eq(SpotifyArtistDTO.class)))
                        .thenReturn(listOfArtist);

        List<SpotifyArtistDTO> resultList = spotifyController.getMyArtist(authorizationHeader);

        assertNotNull(resultList);

        assertEquals(2, resultList.size());

        assertEquals("Artist-1", resultList.get(0).getName());
        assertEquals("Artist-2", resultList.get(1).getName());
    }

    @Test
    void testGetMyPlaylists() {
        Set<SpotifyPlaylistsDTO> listOfPlaylist = Set.of(new SpotifyPlaylistsDTO("1", "Playlist-1"),
                new SpotifyPlaylistsDTO("1", "Playlist-2"));

        when(spotifyService.getOfUsersPlaylists(authorizationHeader))
                .thenReturn(listOfPlaylist);

        Set<SpotifyPlaylistsDTO> resultList = spotifyController.getMyPlaylists(authorizationHeader);

        assertNotNull(resultList);

        assertEquals(2, resultList.size());

        assertTrue(resultList.stream()
                .anyMatch(playlist -> playlist.getName().equals("Playlist-1")));
        assertTrue(resultList.stream()
                .anyMatch(playlist -> playlist.getName().equals("Playlist-2")));
        assertFalse(resultList.stream()
                .anyMatch(playlist -> playlist.getName().equals("Playlist")));
    }

    @Test
    void testGerReleasesByLastTenDays() {
        List<SpotifyReleaseDTO> listOfReleases = Arrays.asList(
                new SpotifyReleaseDTO("1", "Release-1"),
                new SpotifyReleaseDTO("2", "Release-2"),
                new SpotifyReleaseDTO("3", "Release-3")
        );

        when(spotifyService.getReleases(eq(authorizationHeader), eq(releaseOfDay), eq(SpotifyReleaseDTO.class)))
                .thenReturn(listOfReleases);

        List<SpotifyReleaseDTO> result = spotifyController
                .getReleasesByPeriod(releaseOfDay, authorizationHeader);

        assertNotNull(result);
        assertTrue(result.stream()
                .anyMatch(release -> release.getName().equals("Release-3")));
    }

    @Test
    void testSaveReleasesToPlaylist() {
        List<String> resultList = new ArrayList<>();

        List<String> listOfTrack = List.of("track-1", "track-2", "track-3");

        when(spotifyService.saveReleasesToPlaylistById(authorizationHeader, playlistId, releaseOfDay))
                .thenAnswer(call -> {
                    resultList.addAll(listOfTrack);
                    return resultList.size();
                });

        ResponseEntity<Integer> response = spotifyController
                .saveReleasesToPlaylist(playlistId, releaseOfDay, authorizationHeader);

        assertNotNull(response);

        assertFalse(resultList.isEmpty());
        assertEquals(3, resultList.size());
        assertTrue(resultList.stream()
                .anyMatch(track -> track.equals("track-1")));
        assertTrue(resultList.containsAll(listOfTrack));

        verify(spotifyService, times(1))
                .saveReleasesToPlaylistById(authorizationHeader, playlistId, releaseOfDay);
    }

    @Test
    void testDeleteAllOfTrackFromPlaylistById_withSuccessfulStatus() {
        when(spotifyService.deleteAllOfTracksFromPlaylistById(authorizationHeader, playlistId))
                .thenReturn(200);

        ResponseEntity<Integer> result =
                spotifyController.deleteAllItemsFromPlaylistById(playlistId, authorizationHeader);

        assertNotNull(result);

        assertEquals(200, result.getBody());
    }

    @Test
    void testDeleteAllOfTrackFromPlaylistById_withNoContentStatus() {
        when(spotifyService.deleteAllOfTracksFromPlaylistById(authorizationHeader, playlistId))
                .thenReturn(204);

        ResponseEntity<Integer> result =
                spotifyController.deleteAllItemsFromPlaylistById(playlistId, authorizationHeader);

        assertNotNull(result);

        assertEquals(204, result.getBody());
    }
}
