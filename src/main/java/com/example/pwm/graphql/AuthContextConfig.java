// src/main/java/com/example/pwm/graphql/AuthContextConfig.java
package com.example.pwm.graphql;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.http.HttpHeaders;

@Configuration
public class AuthContextConfig {
    @Bean
    WebGraphQlInterceptor authHeaderToContext() {
        return (webInput, chain) -> {
            String auth = webInput.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (auth != null && !auth.isBlank()) {
                webInput.configureExecutionInput((exec, builder) ->
                        builder.graphQLContext(ctx -> ctx.put("Authorization", auth)).build()
                );
            }
            return chain.next(webInput);
        };
    }
}
