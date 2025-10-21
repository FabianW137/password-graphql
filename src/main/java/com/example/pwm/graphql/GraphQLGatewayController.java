package com.example.pwm.graphql;

import com.example.pwm.graphql.service.VaultService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Controller
public class GraphQLGatewayController {

    private final VaultService service;

    public GraphQLGatewayController(VaultService service) {
        this.service = service;
    }

    private UUID requireOwnerId(String authHeader, String xUserId) {
        try {
            if (xUserId != null && !xUserId.isBlank()) {
                return UUID.fromString(xUserId.trim());
            }
            if (authHeader != null && !authHeader.isBlank()) {
                String raw = authHeader.trim();
                if (raw.toLowerCase().startsWith("bearer ")) {
                    raw = raw.substring(7).trim();
                }
                // Erwartet hier einen UUID-String (falls ihr JWT nutzt, bitte auf euren Parser umbauen)
                return UUID.fromString(raw);
            }
        } catch (Exception ignore) { }
        throw new IllegalArgumentException("Owner-ID fehlt oder ist ungültig (Header 'X-User-Id' oder 'Authorization: Bearer <uuid>').");
    }

    @QueryMapping
    public Mono<Dtos.Viewer> viewer(
            @ContextValue(name = "Authorization", required = false) String authHeader,
            @ContextValue(name = "X-User-Id", required = false) String xUserId
    ) {
        UUID owner = requireOwnerId(authHeader, xUserId);
        return Mono.just(new Dtos.Viewer(owner.toString()));
    }

    @QueryMapping
    public Flux<Dtos.VaultItem> vaultItems(
            @ContextValue(name = "Authorization", required = false) String authHeader,
            @ContextValue(name = "X-User-Id", required = false) String xUserId
    ) {
        UUID owner = requireOwnerId(authHeader, xUserId);
        return service.listByOwner(owner);
    }

    @QueryMapping
    public Mono<Dtos.VaultItem> vaultItem(
            @Argument Long id,
            @ContextValue(name = "Authorization", required = false) String authHeader,
            @ContextValue(name = "X-User-Id", required = false) String xUserId
    ) {
        UUID owner = requireOwnerId(authHeader, xUserId);
        return service.getById(owner, id);
    }

    @MutationMapping
    public Mono<Dtos.VaultItem> createVaultItem(
            @Argument Dtos.VaultUpsertEncInput input,
            @ContextValue(name = "Authorization", required = false) String authHeader,
            @ContextValue(name = "X-User-Id", required = false) String xUserId
    ) {
        UUID owner = requireOwnerId(authHeader, xUserId);
        return service.create(owner, input);
    }

    @MutationMapping
    public Mono<Dtos.VaultItem> updateVaultItem(
            @Argument Long id,
            @Argument Dtos.VaultUpsertEncInput input,
            @ContextValue(name = "Authorization", required = false) String authHeader,
            @ContextValue(name = "X-User-Id", required = false) String xUserId
    ) {
        UUID owner = requireOwnerId(authHeader, xUserId);
        return service.update(owner, id, input);
    }

    @MutationMapping
    public Mono<Boolean> deleteVaultItem(
            @Argument Long id,
            @ContextValue(name = "Authorization", required = false) String authHeader,
            @ContextValue(name = "X-User-Id", required = false) String xUserId
    ) {
        UUID owner = requireOwnerId(authHeader, xUserId);
        return service.delete(owner, id);
    }
}
