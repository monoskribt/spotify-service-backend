package com.spotifyapi.props;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "rabbitmq")
public record RabbitMQProperties(String host, String exchange, String queue, String routingKey) {
}