package com.example.pwm.graphql;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class VaultApiClient {
    private final WebClient web;

    public VaultApiClient(WebClient backendWebClient) {
        this.web = backendWebClient;
    }

    private WebClient.RequestHeadersSpec<?> withAuth(WebClient.RequestHeadersSpec<?> spec, String bearer) {
                if (bearer != null && !bearer.isBlank()) {
                        spec = spec.header(HttpHeaders.AUTHORIZATION, bearer);
                  }
                return spec;
            }

            // Für POST/PUT/PATCH (behält RequestBodySpec, damit .contentType/.bodyValue verfügbar bleiben)
           private WebClient.RequestBodySpec withAuth(WebClient.RequestBodySpec spec, String bearer) {
                if (bearer != null && !bearer.isBlank()) {
                        spec = spec.header(HttpHeaders.AUTHORIZATION, bearer);
                    }
                return spec;
            }

    public List<Dtos.VaultItem> list(String bearer) {
        try {
            return withAuth(web.get().uri("/api/vault"), bearer)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToFlux(Dtos.VaultItem.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            throw new RuntimeException(ex.getResponseBodyAsString(), ex);
        }
    }

    public Dtos.VaultItem get(Long id, String bearer) {
        try {
            return withAuth(web.get().uri("/api/vault/{id}", id), bearer)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Dtos.VaultItem.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw new RuntimeException(ex.getResponseBodyAsString(), ex);
        }
    }

    public Dtos.VaultItem create(Dtos.VaultUpsertInput in, String bearer) {
        try {
            return withAuth(web.post().uri("/api/vault"), bearer)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(in)
                    .retrieve()
                    .bodyToMono(Dtos.VaultItem.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw new RuntimeException(ex.getResponseBodyAsString(), ex);
        }
    }

    public Dtos.VaultItem update(Long id, Dtos.VaultUpsertInput in, String bearer) {
        try {
            return withAuth(web.patch().uri("/api/vault/{id}", id), bearer)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(in)
                    .retrieve()
                    .bodyToMono(Dtos.VaultItem.class)
                    .block();
        } catch (WebClientResponseException ex) {
            // optional: fallback to PUT, if backend uses PUT
            try {
                return withAuth(web.put().uri("/api/vault/{id}", id), bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .bodyValue(in)
                        .retrieve()
                        .bodyToMono(Dtos.VaultItem.class)
                        .block();
            } catch (WebClientResponseException ex2) {
                throw new RuntimeException(ex2.getResponseBodyAsString(), ex2);
            }
        }
    }

    public boolean delete(Long id, String bearer) {
        try {
            withAuth(web.delete().uri("/api/vault/{id}", id), bearer)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (WebClientResponseException ex) {
            throw new RuntimeException(ex.getResponseBodyAsString(), ex);
        }
    }
}
