package com.spotifyapi.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "cors")
public record CorsConfigurationProps(String allowedOrigins, List<String> allowedMethods, List<String> allowedHeaders,
                                    boolean allowCredentials, long maxAge) {
}
