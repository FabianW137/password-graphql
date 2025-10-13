package com.example.pwm.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@Controller
public class GraphQLGatewayController {
    private final VaultApiClient client;
    public GraphQLGatewayController(VaultApiClient client) { this.client = client; }

    // Extract JWT subject is optional; here we just expose 'viewer' with a placeholder from the Authorization header.
    @QueryMapping
    public Dtos.Viewer viewer(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        // We don't parse the JWT here; simply return a placeholder. You can decode if you need the UUID.
        String id = authHeader != null ? "token" : "anonymous";
        return new Dtos.Viewer(id);
    }

    @QueryMapping
    public List<Dtos.VaultItem> vaultItems(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        return client.list(authHeader);
    }

    @QueryMapping
    public Dtos.VaultItem vaultItem(@Argument Long id, @RequestHeader(name = "Authorization", required = false) String authHeader) {
        return client.get(id, authHeader);
    }

    @MutationMapping
    public Dtos.VaultItem createVaultItem(@Argument Dtos.VaultUpsertInput input, @RequestHeader(name = "Authorization", required = false) String authHeader) {
        return client.create(input, authHeader);
    }

    @MutationMapping
    public Dtos.VaultItem updateVaultItem(@Argument Long id, @Argument Dtos.VaultUpsertInput input, @RequestHeader(name = "Authorization", required = false) String authHeader) {
        return client.update(id, input, authHeader);
    }

    @MutationMapping
    public Boolean deleteVaultItem(@Argument Long id, @RequestHeader(name = "Authorization", required = false) String authHeader) {
        return client.delete(id, authHeader);
    }
}
