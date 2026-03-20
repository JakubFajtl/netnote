package client.exceptions;

import client.services.LanguageService;
import commons.constants.AppConstants;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

public class ApiResponseValidator implements ResponseValidator {

    private final LanguageService languageService;

    @Inject
    public ApiResponseValidator(LanguageService languageService) {
        this.languageService = languageService;
    }

    /**
     * If the response is not valid, an exception is thrown
     * @param response the response to validate
     */
    public void validateResponse(Response response) {
        if (response == null)
            throw new NotesApiException("",
                    languageService.getDescriptionByKey("Item.responseIsNull"), null);

        // Response statuses that are in range [200, 299] are considered success
        if (response.getStatus() < 200 ||
                response.getStatus() >= 300) {
            var userErrorMessageHeaderValue = response
                    .getHeaders()
                    .get(AppConstants.UserErrorMessageHeader);
            String userMessage = (userErrorMessageHeaderValue != null ?
                    (String)userErrorMessageHeaderValue.getFirst() :
                    "");
            var errorMessageHeaderValue = response
                    .getHeaders()
                    .get(AppConstants.ErrorMessageHeader);
            String message = (errorMessageHeaderValue != null ?
                    (String)errorMessageHeaderValue.getFirst() :
                    languageService.getDescriptionByKey("Item.statuscode")
                            + response.getStatus() + ")");

            String translatedUserMessage = languageService.getDescriptionByKey(userMessage);

            if (response.getStatus() == 409) {
                throw new TitleNotUniqueException(
                        translatedUserMessage
                );
            }

            throw new NotesApiException(
                    message,
                    translatedUserMessage,
                    Response.Status.fromStatusCode(response.getStatus()));
        }
    }
}
