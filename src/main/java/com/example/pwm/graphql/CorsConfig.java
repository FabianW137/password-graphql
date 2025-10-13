package com.example.pwm.graphql;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE) // CORS soll vor allen anderen Filtern laufen
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration cfg = new CorsConfiguration();

        // ⬇️ Trage hier deine erlaubten Origins ein (für Tests permissiv)
        // Tipp Prod: setAllowedOrigins(List.of("https://dein-frontend.example"))
        cfg.setAllowedOriginPatterns(List.of("*"));

        // Alle üblichen Methoden inkl. OPTIONS für Preflight:
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Erlaube alle Request-Header (verhindert 405/403 wegen ungewhitelisteter Header)
        cfg.setAllowedHeaders(List.of("*"));

        // Falls du Cookies/Sessions nutzt -> true UND KEIN "*"-Origin verwenden!
        // Für Bearer-Token reicht in der Regel false.
        cfg.setAllowCredentials(false);

        // Optional: nützliche Response-Header exponieren
        cfg.setExposedHeaders(List.of("Location", "Link"));

        // Preflight-Cache im Browser
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Global auf alle Pfade anwenden, inkl. /graphql
        source.registerCorsConfiguration("/**", cfg);

        return new CorsWebFilter(source);
    }
}
