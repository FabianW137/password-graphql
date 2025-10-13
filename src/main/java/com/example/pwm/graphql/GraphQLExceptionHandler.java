// src/main/java/com/example/pwm/graphql/GraphQLExceptionHandler.java
package com.example.pwm.graphql;

import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;

@Controller
public class GraphQLExceptionHandler {

    @GraphQlExceptionHandler(ResponseStatusException.class)
    public GraphQLError handleRse(ResponseStatusException ex) {
        return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.BAD_REQUEST)
                .message(ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString())
                .build();
    }

    @GraphQlExceptionHandler(WebClientResponseException.class)
    public GraphQLError handleWebClient(WebClientResponseException ex) {
        ex.getResponseBodyAsString();
        String msg = !ex.getResponseBodyAsString().isBlank()
                ? ex.getResponseBodyAsString()
                : ex.getStatusCode().toString();
        return GraphqlErrorBuilder.newError()
                .errorType(ex.getStatusCode().is4xxClientError() ? ErrorType.BAD_REQUEST : ErrorType.INTERNAL_ERROR)
                .message(msg)
                .build();
    }

    @GraphQlExceptionHandler(Exception.class)
    public GraphQLError handleAll(Exception ex) {
        return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.BAD_REQUEST)
                .message(ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName())
                .build();
    }
}
