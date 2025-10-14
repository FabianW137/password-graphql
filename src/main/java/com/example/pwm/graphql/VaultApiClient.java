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

    private WebClient.RequestHeadersSpec<?> withAuth(WebClient.RequestHeadersSpec<?> spec, String authHeader) {
        if (authHeader != null && !authHeader.isBlank()) {
            // Ensure it is "Bearer <token>"
            String v = authHeader.startsWith("Bearer ") ? authHeader : "Bearer " + authHeader;
            return spec.header(HttpHeaders.AUTHORIZATION, v);
        }
        return spec;
    }

    private Mono<? extends Throwable> mapError(ClientResponse res) {
        return res.bodyToMono(String.class).defaultIfEmpty("")
                .flatMap(body -> {
                    String msg = body;
                    if (msg.isBlank()) msg = "Upstream error: HTTP " + res.statusCode();
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

    public record VaultItem(Long id, String title, String username, String password, String url, String notes,
                            String createdAt, String updatedAt) {}

    public record VaultUpsertInput(String title, String username, String password, String url, String notes) {}

    public Mono<List<VaultItem>> list(String authHeader) {
        return withRetry(
                withAuth(http.get().uri("/api/vault"), authHeader)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, this::mapError)
                        .bodyToFlux(VaultItem.class)
                        .collectList()
        );
    }

    public Mono<VaultItem> get(Long id, String authHeader) {
        return withRetry(
                withAuth(http.get().uri("/api/vault/{id}", id), authHeader)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, this::mapError)
                        .bodyToMono(VaultItem.class)
        );
    }

    public Mono<VaultItem> create(VaultUpsertInput input, String authHeader) {
        return withRetry(
                withAuth(http.post().uri("/api/vault"), authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(input)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, this::mapError)
                        .bodyToMono(VaultItem.class)
        );
    }

    public Mono<VaultItem> update(Long id, VaultUpsertInput input, String authHeader) {
        return withRetry(
                withAuth(http.put().uri("/api/vault/{id}", id), authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(input)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, this::mapError)
                        .bodyToMono(VaultItem.class)
        );
    }

    public Mono<Boolean> delete(Long id, String authHeader) {
        return withRetry(
                withAuth(http.delete().uri("/api/vault/{id}", id), authHeader)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, this::mapError)
                        .toBodilessEntity()
                        .thenReturn(true)
        );
    }
}
