package com.CollabSphere.CollabSphere.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow requests from frontend (change if needed)
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",      // React local
                "http://localhost:4200",      // Angular local
                "http://127.0.0.1:3000"
        ));

        // Allowed HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Allow headers used by JWT + File uploads
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With"
        ));

        // Allow frontend to read JWT response headers
        config.setExposedHeaders(List.of(
                "Authorization",
                "Content-Disposition"
        ));

        // Allow sending cookies (optional)
        config.setAllowCredentials(true);

        // Preflight cache duration
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}