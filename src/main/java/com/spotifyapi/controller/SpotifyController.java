package com.spotifyapi.controller;

import com.spotifyapi.dto.spotify_entity.SpotifyArtistDTO;
import com.spotifyapi.dto.spotify_entity.SpotifyPlaylistsDTO;
import com.spotifyapi.dto.spotify_entity.SpotifyReleaseDTO;
import com.spotifyapi.service.SpotifyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/spotify")
@AllArgsConstructor
@Slf4j
public class SpotifyController {

    private SpotifyService spotifyService;

    @GetMapping("/artists")
    public List<SpotifyArtistDTO> getMyArtist(@RequestHeader(value = "Authorization") String authorizationHeader) {
        return spotifyService.getFollowedArtist(authorizationHeader, SpotifyArtistDTO.class);
    }


    @GetMapping("/playlists")
    public Set<SpotifyPlaylistsDTO> getMyPlaylists(
            @RequestHeader(value = "Authorization") String authorizationHeader) {
        return spotifyService.getOfUsersPlaylists(authorizationHeader);
    }

    @GetMapping("/releases")
    public List<SpotifyReleaseDTO> getReleasesByPeriod(
            @RequestParam (value = "releaseOfDay", required = false) Long releaseOfDay,
            @RequestHeader(value = "Authorization") String authorizationHeader) {
        return spotifyService.getReleases(authorizationHeader, releaseOfDay, SpotifyReleaseDTO.class);
    }

    @PostMapping("/playlists/{playlistId}/releases")
    public ResponseEntity<Integer> saveReleasesToPlaylist(@PathVariable ("playlistId") String playlistId,
                                                          @RequestParam ("releaseOfDay") Long releaseOfDay,
                                                          @RequestHeader(value = "Authorization") String authorizationHeader) {
        int result = spotifyService.saveReleasesToPlaylistById(authorizationHeader, playlistId, releaseOfDay);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result);
    }

    @DeleteMapping("/playlists/{playlistId}/items")
    public ResponseEntity<Integer> deleteAllItemsFromPlaylistById(@PathVariable("playlistId") String playlistId,
                                                                  @RequestHeader(value = "Authorization") String authorizationHeader) {
        int result = spotifyService.deleteAllOfTracksFromPlaylistById(authorizationHeader, playlistId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result);
    }
}