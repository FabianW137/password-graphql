package com.example.pwm.graphql;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Controller;

@Controller
public class GraphQLExceptionHandler {
    @GraphQlExceptionHandler(RuntimeException.class)
    public GraphQLError handle(RuntimeException ex) {
        return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.BAD_REQUEST)
                .message(ex.getMessage() == null ? "Backend error" : ex.getMessage())
                .build();
    }
}
