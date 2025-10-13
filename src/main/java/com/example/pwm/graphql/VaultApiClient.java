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

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Component
public class VaultApiClient {

    private final WebClient http;

    public VaultApiClient(
            WebClient.Builder builder,
            // robustes Binding: ENV BACKEND_BASE_URL > backend.base-url > default localhost
            @Value("${BACKEND_BASE_URL:${backend.base-url:http://localhost:8081}}") String baseUrl
    ) {
        this.http = builder
                .baseUrl(baseUrl)
                .defaultHeaders(h -> h.setAccept(List.of(MediaType.APPLICATION_JSON)))
                .build();
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

    private boolean isTransient(Throwable t) {
        if (t instanceof WebClientResponseException wcre) {
            int s = wcre.getStatusCode().value();
            return s == 502 || s == 503 || s == 504;
        }
        if (t instanceof WebClientRequestException) return true; // DNS/Connect/Timeout
        return t instanceof IOException;
    }

    private <T> Mono<T> withRetry(Mono<T> mono) {
        // kurze Backoff-Retries bügeln Render-Coldstarts und kurze Hiccups aus
        return mono.retryWhen(
                Retry.backoff(2, Duration.ofMillis(250))
                        .maxBackoff(Duration.ofSeconds(2))
                        .filter(this::isTransient)
        );
    }

    public Mono<List<Dtos.VaultItem>> list(String authHeader) {
        return withRetry(
                withAuth(http.get().uri("/api/vault"), authHeader)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, this::mapError)
                        .bodyToFlux(Dtos.VaultItem.class)
                        .collectList()
        );
    }

    public Mono<Dtos.VaultItem> get(Long id, String authHeader) {
        return withRetry(
                withAuth(http.get().uri("/api/vault/{id}", id), authHeader)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, this::mapError)
                        .bodyToMono(Dtos.VaultItem.class)
        );
    }

    public Mono<Dtos.VaultItem> create(Dtos.VaultUpsertInput input, String authHeader) {
        return withRetry(
                withAuth(
                        http.post().uri("/api/vault")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(input),
                        authHeader)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, this::mapError)
                        .bodyToMono(Dtos.VaultItem.class)
        );
    }

    public Mono<Dtos.VaultItem> update(Long id, Dtos.VaultUpsertInput input, String authHeader) {
        return withRetry(
                withAuth(
                        http.put().uri("/api/vault/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(input),
                        authHeader)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, this::mapError)
                        .bodyToMono(Dtos.VaultItem.class)
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
