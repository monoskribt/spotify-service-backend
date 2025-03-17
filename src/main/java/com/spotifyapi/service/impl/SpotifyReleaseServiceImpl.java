package com.spotifyapi.service.impl;

import com.spotifyapi.model.SpotifyRelease;
import com.spotifyapi.model.User;
import com.spotifyapi.repository.ReleaseRepository;
import com.spotifyapi.service.RabbitMQService;
import com.spotifyapi.service.SpotifyReleaseService;
import com.spotifyapi.service.SpotifyService;
import com.spotifyapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotifyReleaseServiceImpl implements SpotifyReleaseService {

    private final ReleaseRepository releaseRepository;
    private final SpotifyService spotifyService;
    private final RabbitMQService rabbitMQService;
    private final UserService userService;

    @Override
    public void save(Set<SpotifyRelease> releaseList) {
        releaseRepository.saveAll(releaseList);
    }

    private List<SpotifyRelease> getReleasesByUserId(String id) {
        return releaseRepository.findByUserId(id);
    }

    @Override
    public void checkReleasesForAllUsers() {
        Set<User> userList = userService.getAllUsersWithSubscribeStatus();
        log.info("is working method checkReleasesForAllUsers");

        Map<User, Set<SpotifyRelease>> userReleases = userList.stream()
                .collect(Collectors.toMap(
                        user -> user,
                        this::checkReleasesForUser
                ));

        userReleases.entrySet().stream()
                .filter(release -> !release.getValue().isEmpty())
                .forEach(notification -> rabbitMQService
                        .sendMessageToTelegram(notification.getKey(), notification.getValue()));
    }

    private Set<SpotifyRelease> checkReleasesForUser(User user) {
        String authorizationHeader = userService.getAccessTokenFromDB(user);
        log.info("is working method checkReleasesForUser with token: {}", authorizationHeader );
        List<AlbumSimplified> albumList = spotifyService.getReleases(authorizationHeader);

        List<String> alreadyContainsReleasesId = getAlreadyContainsReleasesId(user);

        Set<SpotifyRelease> checkListRelease = albumList.stream()
                .filter(release -> !alreadyContainsReleasesId.contains(release.getId()))
                .map(release -> convertToSpotifyReleaseEntity(user, release))
                .collect(Collectors.toSet());
        save(checkListRelease);
        return checkListRelease;
    }

    private List<String> getAlreadyContainsReleasesId(User user) {
        return getReleasesByUserId(user.getId())
                .stream()
                .map(SpotifyRelease::getId)
                .toList();
    }

    private SpotifyRelease convertToSpotifyReleaseEntity(User user, AlbumSimplified release) {
        SpotifyRelease spotifyRelease = new SpotifyRelease();
        spotifyRelease.setId(release.getId());
        spotifyRelease.setName(release.getName());
        spotifyRelease.setLocalDate(LocalDate.now());
        spotifyRelease.setUser(user);
        return spotifyRelease;
    }
}
