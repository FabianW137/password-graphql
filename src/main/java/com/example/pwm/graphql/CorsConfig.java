package com.example.pwm.graphql;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter(
            @Value("${app.cors.allowed-origin-patterns:*}") String originPatternsCsv,
            @Value("${app.cors.allow-credentials:false}") boolean allowCredentials,
            @Value("${app.cors.max-age-seconds:86400}") long maxAgeSeconds
    ) {
        CorsConfiguration cfg = new CorsConfiguration();

        // Patterns aus Properties einlesen (CSV -> List)
        List<String> originPatterns = Arrays.stream(originPatternsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        // Wichtig: Bei allowCredentials=true keine "*" Origins verwenden.
        // allowedOriginPatterns erlaubt Wildcards wie "https://*.onrender.com"
        cfg.setAllowedOriginPatterns(originPatterns);

        // GraphQL nutzt idR GET/POST; OPTIONS für Preflight
        cfg.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        // Erlaube alle angefragten Header (z.B. Authorization, apollo-* etc.)
        cfg.setAllowedHeaders(List.of("*"));
        // Nützliche Response-Header für den Browser freigeben
        cfg.setExposedHeaders(List.of("Location", "Link", "Content-Disposition", "Authorization"));

        cfg.setAllowCredentials(allowCredentials);
        cfg.setMaxAge(Duration.ofSeconds(maxAgeSeconds));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Falls du es enger fassen willst, kannst du hier auf "/graphql" und "/graphiql/**" einschränken.
        source.registerCorsConfiguration("/**", cfg);

        return new CorsWebFilter(source);
    }
}
