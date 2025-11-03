package com.example.pwm.graphql;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration cfg = new CorsConfiguration();


        cfg.setAllowedOriginPatterns(List.of(
                "https://passwortmanager2.onrender.com",
                "https://spa-angular-1.onrender.com",
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));


        cfg.setAllowCredentials(false);


        cfg.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));


        cfg.setAllowedHeaders(List.of(
                "Content-Type", "Authorization", "X-User-Id",
                "X-Requested-With", "Accept", "Origin"
        ));


        cfg.setExposedHeaders(List.of("Content-Type"));


        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return new CorsWebFilter(src);
    }
}
