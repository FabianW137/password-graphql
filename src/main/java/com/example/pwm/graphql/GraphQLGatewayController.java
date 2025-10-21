package com.example.pwm.graphql;

import com.example.pwm.graphql.service.VaultService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.UUID;

@Controller
public class GraphQLGatewayController {

    private final VaultService service;

    public GraphQLGatewayController(VaultService service) {
        this.service = service;
    }

    // --- Queries ---

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
        // HIER war der Fehler: VaultService hat listByOwner(UUID), nicht list(UUID)
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

    // --- Mutations ---

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

    // --- Owner-Ermittlung ---

    /**
     * Verwendet bevorzugt die (vom Interceptor gesetzte) X-User-Id.
     * Fällt zurück auf Extraktion aus Authorization (UUID-Token oder JWT-Claim sub|userId|user_id|uid).
     */
    private static UUID requireOwnerId(String authHeader, String xUserId) {
        if (xUserId != null && !xUserId.isBlank()) {
            try { return UUID.fromString(xUserId.trim()); }
            catch (IllegalArgumentException ignore) { /* fallback auf authHeader */ }
        }
        UUID extracted = extractOwnerIdFromAuthorization(authHeader);
        if (extracted != null) return extracted;

        throw new IllegalArgumentException("Owner-ID fehlt oder ist ungültig (Header 'X-User-Id' oder 'Authorization: Bearer <uuid>').");
    }

    private static UUID extractOwnerIdFromAuthorization(String auth) {
        if (auth == null || auth.isBlank()) return null;
        if (!auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
            auth = "Bearer " + auth.trim();
        }
        String token = auth.substring(7).trim();

        // Reiner UUID-Token?
        try { return UUID.fromString(token); } catch (IllegalArgumentException ignore) {}

        // JWT?
        int d1 = token.indexOf('.');
        int d2 = token.indexOf('.', d1 + 1);
        if (d1 > 0 && d2 > d1) {
            try {
                String payloadB64 = token.substring(d1 + 1, d2);
                byte[] json = Base64.getUrlDecoder().decode(padB64(payloadB64));
                String s = new String(json);
                for (String claim : new String[]{"sub", "user_id", "userId", "uid"}) {
                    String value = findJsonStringValue(s, claim);
                    if (value != null) {
                        try { return UUID.fromString(value.trim()); } catch (IllegalArgumentException ignore) {}
                    }
                }
            } catch (Exception ignore) {}
        }
        return null;
    }

    // sehr einfache Claim-Extraktion für String-Werte: "key":"value"
    private static String findJsonStringValue(String json, String key) {
        String needle = "\"" + key + "\"";
        int i = json.indexOf(needle);
        if (i < 0) return null;
        int colon = json.indexOf(':', i + needle.length());
        if (colon < 0) return null;
        int q1 = json.indexOf('"', colon + 1);
        if (q1 < 0) return null;
        int q2 = json.indexOf('"', q1 + 1);
        if (q2 < 0) return null;
        return json.substring(q1 + 1, q2);
    }

    private static String padB64(String s) {
        int mod = s.length() % 4;
        if (mod == 2) return s + "==";
        if (mod == 3) return s + "=";
        if (mod == 1) return s + "===";
        return s;
    }
}
