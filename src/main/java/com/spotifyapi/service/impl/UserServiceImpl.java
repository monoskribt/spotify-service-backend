package com.spotifyapi.service.impl;


import com.spotifyapi.dto.TokensDTO;
import com.spotifyapi.dto.UserInfoDTO;
import com.spotifyapi.enums.SubscribeStatus;
import com.spotifyapi.exception.PlaylistNotFoundException;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            User newUser = buildUser(tokens);

            userInfoDTO.setUserId(newUser.getId());
            userInfoDTO.setNickname(newUser.getUsername());

            userRepository.save(newUser);

            List<PlaylistSimplified> playlists = Arrays.stream(spotifyApi
                    .getListOfCurrentUsersPlaylists()
                    .build()
                    .execute().getItems())
                    .toList();
            List<SpotifyTrackFromPlaylist> saveTrackToDb = playlists.stream()
                    .peek(playlist -> playlistService.savePlaylistToDatabase(playlist, newUser))
                            .map(playlist -> playlistRepository.findById(playlist.getId())
                                    .orElseThrow(() -> new PlaylistNotFoundException("Playlist not found after save")))
                                    .flatMap(spotifyUserPlaylist -> {
                                        try {
                                            return Arrays.stream(
                                                    spotifyApi.getPlaylistsItems(spotifyUserPlaylist.getId())
                                                            .build()
                                                            .execute()
                                                            .getItems()
                                            )
                                                    .filter(Objects::nonNull)
                                                    .map(trackItem -> (Track) trackItem.getTrack())
                                                    .map(track -> spotifyTrackService.convertTrackToTrackDBEntity(new TrackWrapper(track), spotifyUserPlaylist));
                                        } catch (IOException | SpotifyWebApiException | ParseException e) {
                                            log.warn("Error fetching playlist tracks: {}", e.getMessage());
                                            return Stream.empty();
                                        }
                                    }).toList();

            trackRepository.saveAll(saveTrackToDb);

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.warn("Error while saving of user data: {}", e.getMessage());
            throw new SpotifyApiException("Error while saving of user data: " + e.getMessage());
        }
    }

    private User buildUser(TokensDTO tokens) {
        User newUser = new User();
        se.michaelthelin.spotify.model_objects.specification.User userProfile;
        try {
            userProfile = spotifyApi.getCurrentUsersProfile().build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.warn("Error getting of user profile: {}", e.getMessage());
            throw new SpotifyApiException("Error getting of user profile: " + e.getMessage());
        }

        newUser.setUsername(userProfile.getDisplayName());
        newUser.setEmail(userProfile.getEmail());
        newUser.setId(userProfile.getId());
        newUser.setAccessToken("access_token " + tokens.getAccessToken());
        newUser.setRefreshToken(tokens.getRefreshToken());

        newUser.setExpiresAccessTokenAt(Instant.now().plusSeconds(ONE_HOUR));
        newUser.setExpiresRefreshTokenAt(Instant.now().plusSeconds(ONE_WEEK));

        return newUser;
    }

    @Override
    public void updateUserData(TokensDTO tokensDTO) {
        User user;
        try {
            user = userRepository.findById(spotifyApi.getCurrentUsersProfile().build().execute().getId())
                    .orElseThrow(() -> new UserNotFoundException("User not found in DB"));
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.warn("Error during of updating of user data: {}", e.getMessage());
            throw new SpotifyApiException("Error during of updating of user data: " + e.getMessage());
        }

        user.setAccessToken("access_token " + tokensDTO.getAccessToken());
        user.setRefreshToken(tokensDTO.getRefreshToken());

        user.setExpiresAccessTokenAt(Instant.now().plusSeconds(ONE_HOUR));
        user.setExpiresRefreshTokenAt(Instant.now().plusSeconds(ONE_WEEK));

        List<PlaylistSimplified> getAllPlaylists;
        try {
            getAllPlaylists = Arrays.stream(spotifyApi
                    .getListOfCurrentUsersPlaylists().build().execute().getItems()).toList();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.warn("Error during of getting of user playlist: {}", e.getMessage());
            throw new SpotifyApiException("Error during of getting of user playlist: " + e.getMessage());
        }

        List<SpotifyTrackFromPlaylist> newTracks = new ArrayList<>();

        for(PlaylistSimplified playlist : getAllPlaylists) {
            Optional<SpotifyUserPlaylist> existingPlaylist = playlistRepository.findById(playlist.getId());

            if(existingPlaylist.isPresent()) {
                SpotifyUserPlaylist currentPlaylist = existingPlaylist.get();
                Set<String> playlistTracksUrl = spotifyTrackService.getExistingTrackIds(currentPlaylist.getId());
                Paging<PlaylistTrack> tracksFromPlaylist;
                try {
                    tracksFromPlaylist = spotifyApi.getPlaylistsItems(currentPlaylist.getId())
                            .build().execute();
                } catch (IOException | SpotifyWebApiException | ParseException e) {
                    log.warn("Error getting of playlist items: {}", e.getMessage());
                    throw new SpotifyApiException("Error getting of playlist items: " + e.getMessage());
                }

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

    @Override
    public String getCurrentUsername() {
        se.michaelthelin.spotify.model_objects.specification.User profile;
        try {
            profile = spotifyApi.getCurrentUsersProfile().build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.warn("Error getting of username users: {}", e.getMessage());
            throw new SpotifyApiException("Error getting of username users: " + e.getMessage());
        }
        return profile.getDisplayName();
    }

    @Override
    public String getCurrentId() {
        se.michaelthelin.spotify.model_objects.specification.User profile;
        try {
            profile = spotifyApi.getCurrentUsersProfile().build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.warn("Error getting of id users: {}", e.getMessage());
            throw new SpotifyApiException("Error getting of id users: " + e.getMessage());
        }
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

    @Override
    public boolean isAlreadyExist() {
        try {
            return userRepository.existsByEmail(spotifyApi
                    .getCurrentUsersProfile()
                    .build()
                    .execute().getEmail());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.warn("Error during getting of user email: {}", e.getMessage());
            throw new SpotifyApiException("Error during getting of user email: " + e.getMessage());
        }
    }
}
