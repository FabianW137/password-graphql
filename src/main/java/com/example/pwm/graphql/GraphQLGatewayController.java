package com.example.pwm.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
public class GraphQLGatewayController {

    private final VaultApiClient client;

    public GraphQLGatewayController(VaultApiClient client) {
        this.client = client;
    }

    @QueryMapping
    public Mono<List<Dtos.VaultItem>> vaultItems(
            @ContextValue(name = "Authorization", required = false) String authHeader) {
        return client.list(authHeader);
    }

    @MutationMapping
    public Mono<Dtos.VaultItem> createVaultItem(
            @Argument Dtos.VaultUpsertInput input,
            @ContextValue(name = "Authorization", required = false) String authHeader) {
        return client.create(input, authHeader);
    }

    @MutationMapping
    public Mono<Dtos.VaultItem> updateVaultItem(
            @Argument Long id,
            @Argument Dtos.VaultUpsertInput input,
            @ContextValue(name = "Authorization", required = false) String authHeader) {
        return client.update(id, input, authHeader);
    }

    @MutationMapping
    public Mono<Boolean> deleteVaultItem(
            @Argument Long id,
            @ContextValue(name = "Authorization", required = false) String authHeader) {
        return client.delete(id, authHeader);
    }
}
