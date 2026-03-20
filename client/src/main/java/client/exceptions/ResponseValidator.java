package client.exceptions;

import jakarta.ws.rs.core.Response;

public interface ResponseValidator {
    void validateResponse(Response response);
}
