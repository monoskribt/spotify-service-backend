package com.spotifyapi;

import com.spotifyapi.props.CorsConfigurationProps;
import com.spotifyapi.props.RabbitMQProperties;
import com.spotifyapi.props.SpotifyProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({RabbitMQProperties.class, SpotifyProps.class, CorsConfigurationProps.class})
@EnableCaching
public class SpotifyApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpotifyApiApplication.class, args);
    }

}
