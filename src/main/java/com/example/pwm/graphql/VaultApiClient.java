package com.example.pwm.graphql;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Component
public class VaultApiClient {

    private final WebClient http;

    public VaultApiClient(WebClient.Builder builder,
                          @Value("${backend.base-url}") String baseUrl) {
        this.http = builder
                .baseUrl(baseUrl)
                .defaultHeaders(h -> h.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .build();
    }

    private void setAuthHeader(HttpHeaders headers, String authHeader) {
        if (authHeader != null && !authHeader.isBlank()) {
            String v = authHeader.startsWith("Bearer ") ? authHeader : "Bearer " + authHeader;
            headers.set(HttpHeaders.AUTHORIZATION, v);
        }
    }

    private Mono<? extends Throwable> mapError(ClientResponse res) {
        return res.bodyToMono(String.class).defaultIfEmpty("")
                .flatMap(body -> {
                    String msg = (body == null || body.isBlank())
                            ? ("Upstream error: HTTP " + res.statusCode())
                            : body;
                    return Mono.error(new ResponseStatusException(res.statusCode(), msg));
                });
    }

    private <T> Mono<T> withRetry(Mono<T> mono) {
        return mono.retryWhen(
                Retry.backoff(2, Duration.ofMillis(200))
                        .filter(ex -> ex instanceof WebClientRequestException ||
                                (ex instanceof WebClientResponseException w && w.getStatusCode().is5xxServerError()))
        );
    }

    // -------- CRUD --------

    public Mono<List<Dtos.VaultItem>> list(String authHeader) {
        return withRetry(
                http.get()
                        .uri("/api/vault")
                        .headers(h -> setAuthHeader(h, authHeader))
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, this::mapError)
                        .bodyToFlux(Dtos.VaultItem.class)
                        .collectList()
        );
    }

    public Mono<Dtos.VaultItem> get(Long id, String authHeader) {
        return withRetry(
                http.get()
                        .uri("/api/vault/{id}", id)
                        .headers(h -> setAuthHeader(h, authHeader))
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, this::mapError)
                        .bodyToMono(Dtos.VaultItem.class)
        );
    }

    public Mono<Dtos.VaultItem> create(Dtos.VaultUpsertInput input, String authHeader) {
        return withRetry(
                http.post()
                        .uri("/api/vault")
                        .contentType(MediaType.APPLICATION_JSON)      // <- vor bodyValue setzen
                        .headers(h -> setAuthHeader(h, authHeader))   // <- Auth-Header hier injizieren
                        .bodyValue(input)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, this::mapError)
                        .bodyToMono(Dtos.VaultItem.class)
        );
    }

    public Mono<Dtos.VaultItem> update(Long id, Dtos.VaultUpsertInput input, String authHeader) {
        return withRetry(
                http.put()
                        .uri("/api/vault/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(h -> setAuthHeader(h, authHeader))
                        .bodyValue(input)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, this::mapError)
                        .bodyToMono(Dtos.VaultItem.class)
        );
    }

    public Mono<Boolean> delete(Long id, String authHeader) {
        return withRetry(
                http.delete()
                        .uri("/api/vault/{id}", id)
                        .headers(h -> setAuthHeader(h, authHeader))
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, this::mapError)
                        .toBodilessEntity()
                        .thenReturn(true)
        );
    }
}
