package com.example.pwm.graphql;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.graphql.server.*;
import reactor.core.publisher.Mono;

@Component
public class AuthHeaderContextInterceptor implements WebGraphQlInterceptor {

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // Fallback: Wenn der Client nur den nackten JWT sendet, "Bearer " voranstellen
        if (auth != null && !auth.isBlank() && !auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
            // primitive JWT-Erkennung: 3 Teile durch Punkte
            if (auth.chars().filter(ch -> ch == '.').count() == 2) {
                auth = "Bearer " + auth;
            }
        }

        final String value = auth; // effectively final
        request.configureExecutionInput((exec, builder) ->
                builder.graphQLContext(ctx -> {
                    if (value != null && !value.isBlank()) {
                        ctx.put("Authorization", value);
                    }
                }).build()
        );

        return chain.next(request);
    }
}
