package com.spotifyapi.service.impl;


import com.spotifyapi.dto.TokensDTO;
import com.spotifyapi.dto.UserInfoDTO;
import com.spotifyapi.enums.SubscribeStatus;
import com.spotifyapi.exception.SpotifyApiException;
import com.spotifyapi.exception.UserNotFoundException;
import com.spotifyapi.mapper.TrackWrapper;
import com.spotifyapi.model.SpotifyTrackFromPlaylist;
import com.spotifyapi.model.SpotifyUserPlaylist;
import com.spotifyapi.model.User;
import com.spotifyapi.repository.PlaylistRepository;
import com.spotifyapi.repository.TrackRepository;
import com.spotifyapi.repository.UserRepository;
import com.spotifyapi.service.*;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.spotifyapi.constant.ConstantExpireTokenTime.ONE_HOUR;
import static com.spotifyapi.constant.ConstantExpireTokenTime.ONE_WEEK;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final SpotifyApi spotifyApi;
    private final UserRepository userRepository;
    private final SpotifyPlaylistService playlistService;
    private final SpotifyTrackService spotifyTrackService;
    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;
    private final TokenService tokenService;

    @Transactional
    @Override
    public void saveUserOfData(TokensDTO tokens, UserInfoDTO userInfoDTO) {
        try {
            User newUser = new User();

            var userProfile = spotifyApi.getCurrentUsersProfile().build().execute();

            newUser.setUsername(userProfile.getDisplayName());
            newUser.setEmail(userProfile.getEmail());
            newUser.setId(userProfile.getId());
            newUser.setAccessToken("access_token " + tokens.getAccessToken());
            newUser.setRefreshToken(tokens.getRefreshToken());

            newUser.setExpiresAccessTokenAt(Instant.now().plusSeconds(ONE_HOUR));
            newUser.setExpiresRefreshTokenAt(Instant.now().plusSeconds(ONE_WEEK));

            userInfoDTO.setUserId(userProfile.getId());
            userInfoDTO.setNickname(userProfile.getDisplayName());

            userRepository.save(newUser);

            List<PlaylistSimplified> playlists = Arrays.stream(spotifyApi
                    .getListOfCurrentUsersPlaylists()
                    .build()
                    .execute().getItems())
                    .toList();

            List<SpotifyTrackFromPlaylist> saveTrackToDB = new ArrayList<>();

            for (PlaylistSimplified playlist : playlists) {

                playlistService.savePlaylistToDatabase(playlist, newUser);

                SpotifyUserPlaylist spotifyUserPlaylist = playlistRepository.findById(playlist.getId())
                        .orElseThrow(() -> new IllegalStateException("Playlist not found after save"));

                List<PlaylistTrack> playlistTracks = Arrays.stream(
                        spotifyApi.getPlaylistsItems(playlist.getId())
                                .build()
                                .execute()
                                .getItems())
                        .toList();

                for (PlaylistTrack trackItem : playlistTracks) {
                    if(trackItem != null) {
                        Track track = (Track) trackItem.getTrack();

                        SpotifyTrackFromPlaylist trackEntity = spotifyTrackService
                                .convertTrackToTrackDBEntity(new TrackWrapper(track), spotifyUserPlaylist);

                        saveTrackToDB.add(trackEntity);
                    }
                }

            }

            trackRepository.saveAll(saveTrackToDB);


        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Error while saving of user data: {}", e.getMessage());
            throw new SpotifyApiException("Error while saving of user data: " + e.getMessage());
        }
    }


    @SneakyThrows
    @Override
    public void updateUserData(TokensDTO tokensDTO) {
        User user = userRepository.findById(spotifyApi.getCurrentUsersProfile().build().execute().getId())
                .orElseThrow(() -> new UserNotFoundException("User not found in DB"));

        user.setAccessToken("access_token " + tokensDTO.getAccessToken());
        user.setRefreshToken(tokensDTO.getRefreshToken());

        user.setExpiresAccessTokenAt(Instant.now().plusSeconds(ONE_HOUR));
        user.setExpiresRefreshTokenAt(Instant.now().plusSeconds(ONE_WEEK));

        List<PlaylistSimplified> getAllPlaylists = Arrays.stream(spotifyApi
                .getListOfCurrentUsersPlaylists().build().execute().getItems()).toList();

        List<SpotifyTrackFromPlaylist> newTracks = new ArrayList<>();

        for(PlaylistSimplified playlist : getAllPlaylists) {
            Optional<SpotifyUserPlaylist> existingPlaylist = playlistRepository.findById(playlist.getId());

            if(existingPlaylist.isPresent()) {
                SpotifyUserPlaylist currentPlaylist = existingPlaylist.get();
                Set<String> playlistTracksUrl = spotifyTrackService.getExistingTrackIds(currentPlaylist.getId());
                var tracksFromPlaylist = spotifyApi.getPlaylistsItems(currentPlaylist.getId())
                        .build().execute();

                for(PlaylistTrack track : tracksFromPlaylist.getItems()) {
                    if(!playlistTracksUrl.contains(track.getTrack().getId())) {
                        SpotifyTrackFromPlaylist trackEntity =
                                spotifyTrackService
                                        .convertTrackToTrackDBEntity(
                                                new TrackWrapper((Track) track.getTrack()), currentPlaylist);
                        newTracks.add(trackEntity);
                    }
                }
            }
            else {
                playlistService.savePlaylistToDatabase(playlist, user);
            }
        }
        trackRepository.saveAll(newTracks);
        userRepository.save(user);
    }

    @Override
    public void manageSubscribeStatusOfUser(SubscribeStatus status) {
        User user = userRepository.findById(getCurrentId())
                .orElseThrow(() -> new UserNotFoundException("User is not found"));

        user.setSubscribeStatus(status);
        userRepository.save(user);
    }

    @Override
    public Set<User> getAllUsersWithSubscribeStatus() {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getSubscribeStatus().equals(SubscribeStatus.SUBSCRIBE))
                .collect(Collectors.toSet());
    }

    @Override
    public String getSubscribeStatusUsers() {
        User u = userRepository.findById(getCurrentId())
                .orElseThrow(() -> new UserNotFoundException("User is not found"));
        return u.getSubscribeStatus().toString();
    }


    @SneakyThrows
    @Override
    public String getCurrentUsername() {
        var profile = spotifyApi.getCurrentUsersProfile().build().execute();
        return profile.getDisplayName();
    }


    @SneakyThrows
    @Override
    public String getCurrentId() {
        var profile = spotifyApi.getCurrentUsersProfile().build().execute();
        return profile.getId();
    }

    @Override
    public String getAccessTokenFromDB(User user) {
        if (!tokenService.isValidAccessToken(user)) {
            tokenService.getNewAccessToken(user);
            userRepository.save(user);
        }
        return user.getAccessToken();
    }


    @SneakyThrows
    @Override
    public boolean isAlreadyExist() {
         return userRepository.existsByEmail(spotifyApi
                 .getCurrentUsersProfile()
                 .build()
                 .execute().getEmail());
    }
}
