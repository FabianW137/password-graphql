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

        // Falls du NUR deinen Frontend-Host erlauben willst: trage ihn hier ein.
        // Mit Origin-Patterns kannst du Ports/localhost wildkarten.
        cfg.setAllowedOriginPatterns(List.of(
                "https://passwortmanager2.onrender.com",
                "https://spa-angular-1.onrender.com",
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));

        // Wir nutzen keine Cookies/Sessions -> Credentials aus.
        cfg.setAllowCredentials(false);

        // Preflight + reale Methoden
        cfg.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));

        // WICHTIG: eigene Header zulassen (X-User-Id / Authorization)
        cfg.setAllowedHeaders(List.of(
                "Content-Type", "Authorization", "X-User-Id",
                "X-Requested-With", "Accept", "Origin"
        ));

        // Optional: sichtbare Response-Header
        cfg.setExposedHeaders(List.of("Content-Type"));

        // Wie lange der Preflight gecacht werden darf
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return new CorsWebFilter(src);
    }
}
