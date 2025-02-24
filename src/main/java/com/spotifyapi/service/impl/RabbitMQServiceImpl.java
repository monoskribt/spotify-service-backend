package com.spotifyapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotifyapi.dto.spotify_entity.SpotifyReleaseDTO;
import com.spotifyapi.dto.TelegramMessageDTO;
import com.spotifyapi.model.SpotifyRelease;
import com.spotifyapi.model.User;
import com.spotifyapi.service.RabbitMQService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitMQServiceImpl implements RabbitMQService {

    private final RabbitTemplate rabbitTemplate;
    private final Binding binding;
    private final ObjectMapper objectMapper;


    @SneakyThrows
    @Override
    public void sendMessageToTelegram(User user, Set<SpotifyRelease> releases) {
        List<SpotifyReleaseDTO> releaseInfo = releases.stream()
                .map(album -> new SpotifyReleaseDTO(album.getId(), album.getName()))
                .toList();

        TelegramMessageDTO message = new TelegramMessageDTO(user.getEmail(), releaseInfo);

        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend(binding.getExchange(), binding.getRoutingKey(), jsonMessage);
            log.info("Sent message to Rabbit: {}", jsonMessage);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message: {}", e.getMessage(), e);
        }
    }
}

