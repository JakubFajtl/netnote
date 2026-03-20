package server;

import commons.constants.AppConstants;
import org.apache.catalina.connector.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class ErrorResponseBuilderTest {
    ErrorResponseBuilder errorResponseBuilder;

    @BeforeEach
    void setup() {
        errorResponseBuilder = new ErrorResponseBuilder();
    }

    @Test
    void createErrorHeadersTest() {
        String errorMessage = "error message";
        String userMessage = "user message";
        HttpHeaders headers = errorResponseBuilder
                        .createErrorHeaders("error message", "user message");
        assertEquals(errorMessage, headers.get(AppConstants.ErrorMessageHeader).getFirst());
        assertEquals(userMessage, headers.get(AppConstants.UserErrorMessageHeader).getFirst());
    }

    @Test
    void buildBadRequest() {
        String userMessage = "user message";
        var response = errorResponseBuilder.buildBadRequest(userMessage);
        assertEquals(Response.SC_BAD_REQUEST, response.getStatusCode().value());
        assertEquals(
                userMessage,
                response.getHeaders().get(AppConstants.UserErrorMessageHeader).getFirst());
    }

    @Test
    void buildNotFound() {
        String userMessage = "user message";
        var response = errorResponseBuilder.buildNotFound(userMessage);
        assertEquals(Response.SC_NOT_FOUND, response.getStatusCode().value());
        assertEquals(
                userMessage,
                response.getHeaders().get(AppConstants.UserErrorMessageHeader).getFirst());
    }

    @Test
    void buildServerError() {
        var response = errorResponseBuilder.buildServerError(new Exception("message"));
        assertEquals(
                Response.SC_INTERNAL_SERVER_ERROR,
                response.getStatusCode().value());
    }
}