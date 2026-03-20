package server;

import commons.constants.AppConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ErrorResponseBuilder {

    /**
     * Adds headers with error information to the response
     * @param errorMessage the message of the error
     * @param userMessage the user-friendly message of the error
     * @return the headers with added information
     */
    public HttpHeaders createErrorHeaders(String errorMessage, String userMessage) {
        HttpHeaders headers = new HttpHeaders();

        if (errorMessage != null)
            headers.add(AppConstants.ErrorMessageHeader, errorMessage);

        if (userMessage != null)
            headers.add(AppConstants.UserErrorMessageHeader, userMessage);
        return headers;
    }

    public <T> ResponseEntity<T> buildBadRequest(String userMessage) {
        return ResponseEntity
                .badRequest()
                .headers(createErrorHeaders(null, userMessage))
                .build();
    }

    public <T> ResponseEntity<T> buildNotFound(String userMessage) {
        return ResponseEntity
                .notFound()
                .headers(createErrorHeaders(null, userMessage))
                .build();
    }

    /**
     * Creates server error response
     * @param e the exception to check
     * @return response entity with status code 500
     * @param <T> the entity
     */
    public <T> ResponseEntity<T> buildServerError(Exception e) {
        // It is very important to log the exception
        // This way the information is not lost, and we can understand what happened
        System.err.println("Exception was thrown with message: " + e.getMessage());
        e.printStackTrace();

        return ResponseEntity
                .internalServerError()
                .headers(createErrorHeaders(
                        e.getMessage(),
                        "Item.unexpectedServerError"))
                .build();
    }

    public <T> ResponseEntity<T> buildConflict(String userMessage) {
        return ResponseEntity
                .status(409)
                .headers(createErrorHeaders(null, userMessage))
                .build();
    }

}
