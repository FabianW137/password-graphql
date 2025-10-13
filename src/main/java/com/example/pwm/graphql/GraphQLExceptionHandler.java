package com.example.pwm.graphql;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GraphQLExceptionHandler {

    @GraphQlExceptionHandler(ResponseStatusException.class)
    public GraphQLError handleRse(ResponseStatusException ex) {
        String msg = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
        ErrorType type = ex.getStatusCode().is4xxClientError()
                ? ErrorType.BAD_REQUEST
                : ErrorType.INTERNAL_ERROR;
        return GraphqlErrorBuilder.newError()
                .errorType(type)
                .message(msg)
                .build();
    }

    @GraphQlExceptionHandler(WebClientResponseException.BadGateway.class)
    public GraphQLError handleBadGateway(WebClientResponseException.BadGateway ex) {
        return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.INTERNAL_ERROR)
                .message("Backend vorübergehend nicht erreichbar (502). Bitte kurz später erneut versuchen.")
                .build();
    }

    @GraphQlExceptionHandler(WebClientRequestException.class)
    public GraphQLError handleRequest(WebClientRequestException ex) {
        return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.INTERNAL_ERROR)
                .message("Keine Verbindung zum Backend (" + ex.getClass().getSimpleName() + ").")
                .build();
    }

    @GraphQlExceptionHandler(Exception.class)
    public GraphQLError handleAll(Exception ex) {
        return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.INTERNAL_ERROR)
                .message(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage())
                .build();
    }
}
