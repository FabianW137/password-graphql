package com.example.pwm.graphql;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .cors(cors -> {})                 // CORS aktiv
                .csrf(csrf -> csrf.disable())     // CSRF aus für API
                .authorizeExchange(reg -> reg
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // Preflight erlauben
                        .pathMatchers("/graphql", "/graphiql", "/voyager").permitAll()
                        .anyExchange().permitAll()
                )
                .build();
    }
}
