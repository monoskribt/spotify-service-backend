package com.spotifyapi.service.impl;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.spotifyapi.dto.spotify_entity.SpotifyArtistDTO;
import com.spotifyapi.dto.spotify_entity.SpotifyPlaylistsDTO;
import com.spotifyapi.dto.spotify_entity.SpotifyReleaseDTO;
import com.spotifyapi.exception.PlaylistNotFoundException;
import com.spotifyapi.mapper.TrackSimplifiedWrapper;
import com.spotifyapi.model.ProgressArtistsUpdate;
import com.spotifyapi.model.SpotifyArtist;
import com.spotifyapi.model.SpotifyTrackFromPlaylist;
import com.spotifyapi.model.SpotifyUserPlaylist;
import com.spotifyapi.repository.PlaylistRepository;
import com.spotifyapi.repository.TrackRepository;
import com.spotifyapi.service.PaginationService;
import com.spotifyapi.service.SpotifyService;
import com.spotifyapi.service.SpotifyTrackService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.*;


import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.spotifyapi.constant.ConstantDayForReleases.THIRTY_DAYS;

@Service
@AllArgsConstructor
@EnableAsync
@Slf4j
public class SpotifyServiceImpl implements SpotifyService {

    private final SpotifyApi spotifyApi;
    private final PlaylistRepository playlistRepository;
    private final SpotifyTrackService spotifyTrackService;
    private final TrackRepository trackRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final PaginationService paginationService;

    private static final Logger logger = LoggerFactory.getLogger(SpotifyServiceImpl.class);

    @Override
    @SneakyThrows
    @Cacheable(value = "artists", key = "#authorizationHeader")
    public <T> List<T> getFollowedArtist(String authorizationHeader, Class<T> returnTypeOfClass) {
        List<SpotifyArtist> followedArtists = paginationService.paginationOfArtists();

        log.info("working is method 'getFollowedArtist', but not cache");
        return followedArtists.stream()
                .map(artis -> createNewInstanceOfArtist(returnTypeOfClass, artis))
                .collect(Collectors.toList());
    }


    private <T> T createNewInstanceOfArtist(Class<T> returnTypeOf, SpotifyArtist artist) {
        if(returnTypeOf.equals(SpotifyArtist.class)) {
            return returnTypeOf.cast(artist);
        } else if(returnTypeOf.equals(SpotifyArtistDTO.class)) {
            return returnTypeOf.cast(new SpotifyArtistDTO(artist.getName()));
        }

        throw new IllegalArgumentException("Error type of class: " + returnTypeOf.getName());
    }


    @Override
    @SneakyThrows
    @Cacheable(value = "playlists", key = "#authorizationHeader")
    public Set<SpotifyPlaylistsDTO> getOfUsersPlaylists(String authorizationHeader) {
        var listOfPlaylist = Arrays.stream(spotifyApi.getListOfCurrentUsersPlaylists()
                .build()
                .execute().getItems()).collect(Collectors.toSet());

        log.info("working is method 'getOfUsersPlaylists', but not cache");
        return listOfPlaylist.stream()
                .map(playlist -> new SpotifyPlaylistsDTO(playlist.getId(), playlist.getName()))
                .collect(Collectors.toSet());
    }


    @Override
    @SneakyThrows
    public List<AlbumSimplified> getReleases(String authorizationHeader) {
        return getReleases(authorizationHeader, THIRTY_DAYS, AlbumSimplified.class);
    }

    @Override
    @SneakyThrows
    @Cacheable(value = "releases", key = "#authorizationHeader + '_' + #releaseOfDay")
    public <T> List<T> getReleases(String authorizationHeader, Long releaseOfDay,
                                   Class<T> returnTypeOfClass) {
        List<SpotifyArtist> artists = getFollowedArtist(authorizationHeader, SpotifyArtist.class);
        Queue<AlbumSimplified> listOfAlbums = new ConcurrentLinkedQueue<>();

        int totalArtists = artists.size();
        AtomicInteger processedArtistsCounter = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        try {
            List<CompletableFuture<List<AlbumSimplified>>> futures = artists.stream()
                    .map(artist -> CompletableFuture.supplyAsync(() ->
                            processedArtist(releaseOfDay,
                                    artist,
                                    processedArtistsCounter,
                                    totalArtists),
                            executorService)
                    ).toList();


            futures.forEach(future -> listOfAlbums.addAll(future.join()));

        } finally {
            executorService.shutdown();
        }


        log.info("Size of listOfAlbums: {}", listOfAlbums.size());
        log.info("working is method 'getReleases', but not cache");

        return listOfAlbums.stream()
                .map(release -> createNewInstanceOfReleases(returnTypeOfClass, release))
                .collect(Collectors.toList());
    }

    private List<AlbumSimplified> processedArtist(Long releaseOfDay,
                                                  SpotifyArtist artist,
                                                  AtomicInteger processedArtistsCounter,
                                                  int totalArtists) {
        List<AlbumSimplified> albums = paginationService
                .paginationOfReleasesArtist(artist.getId(), releaseOfDay);

        int progress = processedArtistsCounter.incrementAndGet();
        int remaining = totalArtists - progress;
        log.info("Processed artist: {}, remaining: {}", artist.getName(), remaining);
        messagingTemplate.convertAndSend("/topic/progress",
                new ProgressArtistsUpdate(progress, totalArtists));

        return albums;
    }

    private <T> T createNewInstanceOfReleases(Class<T> returnTypeOf, AlbumSimplified album) {
        if(returnTypeOf.equals(AlbumSimplified.class)) {
            return returnTypeOf.cast(album);
        } else if(returnTypeOf.equals(SpotifyReleaseDTO.class)) {
            return returnTypeOf.cast(new SpotifyReleaseDTO(album.getId(), album.getName()));
        }

        throw new IllegalArgumentException("Error type of class: " + returnTypeOf.getName());
    }

    @SneakyThrows
    @Override
    @Transactional
    public int saveReleasesToPlaylistById(String authorizationHeader, String playlistId, Long releaseOfDay) {
        List<AlbumSimplified> releases =
                getReleases(authorizationHeader, releaseOfDay, AlbumSimplified.class);

        List<SpotifyTrackFromPlaylist> saveTrackToDB = new ArrayList<>();

        SpotifyUserPlaylist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist not found"));

        Set<String> existingTrackIds = spotifyTrackService
                .getExistingTrackIds(playlist.getId());


        List<String> trackUrl = releases
                .stream()
                .flatMap(album -> paginationService.paginationOfSaveReleasesMethod(album.getId())
                        .stream())
                .filter(track -> !existingTrackIds.contains(track.getId()))
                .peek(track -> {
                    SpotifyTrackFromPlaylist trackEntity =
                            spotifyTrackService.convertTrackToTrackDBEntity(
                                    new TrackSimplifiedWrapper(track),
                                    playlist);
                    saveTrackToDB.add(trackEntity);
                })
                .map(TrackSimplified::getUri)
                .toList();

        log.info("List with releases 'Save Track To DB' is: {}", saveTrackToDB.size());
        if(saveTrackToDB.isEmpty()) {
            return Response.SC_NO_CONTENT;
        }

        trackRepository.saveAll(saveTrackToDB);

        for (int i = 0; i < trackUrl.size(); i += 50) {
            List<String> trackUrlPart = trackUrl.subList(i, Math.min(i + 50, trackUrl.size()));

            spotifyApi.addItemsToPlaylist(playlistId, trackUrlPart.toArray(new String[0]))
                    .build()
                    .execute();
        }

        return Response.SC_OK;
    }

    @SneakyThrows
    @Override
    @Transactional
    public int deleteAllOfTracksFromPlaylistById(String authorizationHeader, String playlistId) {
        List<PlaylistTrack> allTracks = Optional.ofNullable(paginationService.paginationOfDeleteReleasesMethod(playlistId))
                .orElse(Collections.emptyList());

        if (allTracks.isEmpty()) {
            logger.info("Playlist is already empty");
            return Response.SC_NO_CONTENT;
        }

        JsonArray removeTracks = allTracks.stream()
                .map(track -> {
                    JsonObject trackObj = new JsonObject();
                    trackObj.addProperty("uri", track.getTrack().getUri());
                    return trackObj;
                })
                .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);

        try {
            for(int i = 0; i < removeTracks.size(); i+= 50) {
                JsonArray pathArrayToRemove = new JsonArray();

                for(int j = i; j < Math.min(i + 50, removeTracks.size()); j++) {
                    pathArrayToRemove.add(removeTracks.get(j));
                }

                spotifyApi.removeItemsFromPlaylist(playlistId, pathArrayToRemove)
                        .build()
                        .execute();
            }

            List<SpotifyTrackFromPlaylist> tracksToRemove = trackRepository.findAllByUserPlaylistId(playlistId);
            trackRepository.deleteAll(tracksToRemove);

            return Response.SC_OK;
        } catch (Exception e) {
            logger.error("Error while removing tracks from playlist: {}", e.getMessage());
        }
        return Response.SC_INTERNAL_SERVER_ERROR;
    }
}