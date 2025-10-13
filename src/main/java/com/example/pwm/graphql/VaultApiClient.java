package com.example.pwm.graphql;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class VaultApiClient {

    private final WebClient http;

    public VaultApiClient(WebClient.Builder builder,
                          @Value("${backend.base-url}") String baseUrl) {
        this.http = builder.baseUrl(baseUrl).build();
    }

    private Mono<? extends Throwable> mapError(ClientResponse res) {
        HttpStatusCode sc = res.statusCode();
        return res.bodyToMono(String.class)
                .defaultIfEmpty(sc.toString())
                .map(body -> new ResponseStatusException(sc, body));
    }

    private WebClient.RequestHeadersSpec<?> withAuth(WebClient.RequestHeadersSpec<?> spec, String authHeader) {
        return spec.headers(h -> {
            if (authHeader != null && !authHeader.isBlank()) {
                h.set(HttpHeaders.AUTHORIZATION, authHeader);
            }
        });
    }

    public Mono<List<Dtos.VaultItem>> list(String authHeader) {
        return withAuth(http.get().uri("/api/vault"), authHeader)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToFlux(Dtos.VaultItem.class)
                .collectList();
    }

    public Mono<Dtos.VaultItem> get(Long id, String authHeader) {
        return withAuth(http.get().uri("/api/vault/{id}", id), authHeader)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(Dtos.VaultItem.class);
    }

    public Mono<Dtos.VaultItem> create(Dtos.VaultUpsertInput input, String authHeader) {
        return withAuth(
                http.post().uri("/api/vault")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(input),
                authHeader)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(Dtos.VaultItem.class);
    }

    public Mono<Dtos.VaultItem> update(Long id, Dtos.VaultUpsertInput input, String authHeader) {
        return withAuth(
                http.put().uri("/api/vault/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(input),
                authHeader)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(Dtos.VaultItem.class);
    }

    public Mono<Boolean> delete(Long id, String authHeader) {
        return withAuth(http.delete().uri("/api/vault/{id}", id), authHeader)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .toBodilessEntity()
                .thenReturn(true);
    }
}
