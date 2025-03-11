package com.spotifyapi.config;

import com.spotifyapi.props.CorsConfigurationProps;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final CorsConfigurationProps corsProps;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(corsProps.allowedOrigins())
                .allowedMethods(corsProps.allowedMethods().toArray(new String[0]))
                .allowedHeaders(corsProps.allowedHeaders().toArray(new String[0]))
                .allowCredentials(corsProps.allowCredentials())
                .maxAge(corsProps.maxAge());
    }
}
