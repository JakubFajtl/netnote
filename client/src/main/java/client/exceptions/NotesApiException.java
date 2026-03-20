package client.exceptions;

import jakarta.ws.rs.core.Response;

public class NotesApiException extends RuntimeException {
    // The status code of the http response
    private final Response.Status statusCode;

    // The message to show to the user
    private final String userMessage;

    public NotesApiException(
            String message,
            String userMessage,
            Response.Status statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.userMessage = userMessage;
    }

    public Response.Status getStatusCode() {
        return statusCode;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
