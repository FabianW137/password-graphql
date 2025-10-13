package com.example.pwm.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class GraphQLGatewayController {

    private final VaultApiClient client;

    public GraphQLGatewayController(VaultApiClient client) {
        this.client = client;
    }

    // ----------------------------------------------------------------------
    // Queries
    // ----------------------------------------------------------------------

    @QueryMapping
    public Dtos.Viewer viewer(@ContextValue(name = "Authorization", required = false) String authHeader) {
        String token = extractBearerToken(authHeader);
        String id = (token == null) ? "anonymous" : "viewer";
        return new Dtos.Viewer(id);
    }

    @QueryMapping
    public List<Dtos.VaultItem> vaultItems(@ContextValue(name = "Authorization", required = false) String authHeader) {
        // Token steht bei Bedarf via extractBearerToken(authHeader) zur Verfügung
        return client.list(authHeader);
    }

    @QueryMapping
    public Dtos.VaultItem vaultItem(
            @Argument Long id,
            @ContextValue(name = "Authorization", required = false) String authHeader
    ) {
        return client.get(id, authHeader);
    }

    // ----------------------------------------------------------------------
    // Mutations
    // ----------------------------------------------------------------------

    @MutationMapping
    public Dtos.VaultItem createVaultItem(
            @Argument Dtos.VaultUpsertInput input,
            @ContextValue(name = "Authorization", required = false) String authHeader
    ) {
        // Beispiel: String token = extractBearerToken(authHeader);
        return client.create(input, authHeader);
    }

    @MutationMapping
    public Dtos.VaultItem updateVaultItem(
            @Argument Long id,
            @Argument Dtos.VaultUpsertInput input,
            @ContextValue(name = "Authorization", required = false) String authHeader
    ) {
        // Beispiel: String token = extractBearerToken(authHeader);
        return client.update(id, input, authHeader);
    }

    @MutationMapping
    public Boolean deleteVaultItem(
            @Argument Long id,
            @ContextValue(name = "Authorization", required = false) String authHeader
    ) {
        // Beispiel: String token = extractBearerToken(authHeader);
        return client.delete(id, authHeader);
    }

    // ----------------------------------------------------------------------
    // Helper
    // ----------------------------------------------------------------------

    /**
     * Extrahiert den Bearer-Token aus einem Authorization-Header.
     * Akzeptiert z. B. "Bearer <token>" (Groß-/Kleinschreibung egal) und ignoriert führende/nachfolgende Spaces.
     *
     * @param authHeader z. B. "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * @return der Token ohne "Bearer"-Präfix; oder null bei fehlendem/ungültigem Header
     */
    private static String extractBearerToken(String authHeader) {
        if (authHeader == null) return null;

        int n = authHeader.length();
        int i = 0;

        // führende Whitespaces
        while (i < n && Character.isWhitespace(authHeader.charAt(i))) i++;

        // "Bearer" (case-insensitive) erwarten
        String bearer = "Bearer";
        if (i + bearer.length() > n || !authHeader.regionMatches(true, i, bearer, 0, bearer.length())) {
            return null; // falsches Schema
        }

        i += bearer.length();

        // Whitespaces nach "Bearer"
        while (i < n && Character.isWhitespace(authHeader.charAt(i))) i++;

        if (i >= n) return null; // kein Token vorhanden

        String token = authHeader.substring(i).trim();
        return token.isEmpty() ? null : token;
    }
}
