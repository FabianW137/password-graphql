package com.example.pwm.graphql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.graphql.server.*;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Component
public class AuthHeaderContextInterceptor implements WebGraphQlInterceptor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        
        if (auth != null && !auth.isBlank() && !startsWithBearer(auth)) {
            auth = "Bearer " + auth.trim();
        }

        final String authFinal = auth;
        final UUID owner = extractOwnerId(authFinal); 

        request.configureExecutionInput((exec, builder) ->
                builder.graphQLContext(ctx -> {
                    if (authFinal != null && !authFinal.isBlank()) {
                        ctx.put("Authorization", authFinal);
                    }
                    if (owner != null) {
                        ctx.put("X-User-Id", owner.toString());
                    }
                }).build()
        );

        return chain.next(request);
    }

    private static boolean startsWithBearer(String v) {
        return v.regionMatches(true, 0, "Bearer ", 0, 7);
    }

    
    private static UUID extractOwnerId(String auth) {
        if (auth == null || auth.isBlank() || !startsWithBearer(auth)) return null;

        String token = auth.substring(7).trim();
        try {
            return UUID.fromString(token);
        } catch (IllegalArgumentException ignore) {

        }

        int d1 = token.indexOf('.');
        int d2 = token.indexOf('.', d1 + 1);
        if (d1 > 0 && d2 > d1) {
            try {
                String payloadB64 = token.substring(d1 + 1, d2);
                byte[] json = Base64.getUrlDecoder().decode(padB64(payloadB64));
                JsonNode node = MAPPER.readTree(json);

                for (String claim : new String[]{"sub", "user_id", "userId", "uid"}) {
                    JsonNode v = node.get(claim);
                    if (v != null && !v.isNull()) {
                        String s = v.asText(null);
                        if (s != null && !s.isBlank()) {
                            try { return UUID.fromString(s.trim()); } catch (IllegalArgumentException ignore) {}
                        }
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    private static String padB64(String s) {
        int mod = s.length() % 4;
        if (mod == 2) return s + "==";
        if (mod == 3) return s + "=";
        if (mod == 1) return s + "==="; 
        return s;
    }
}
