package client.exceptions;

import client.services.LanguageService;
import client.utils.HealthCheckClient;
import commons.CollectionConfig;
import jakarta.inject.Inject;

public class ServerValidator {
    private final HealthCheckClient healthCheckClient;
    private final LanguageService languageService;

    @Inject
    public ServerValidator(HealthCheckClient healthCheckClient, LanguageService languageService) {
        this.healthCheckClient = healthCheckClient;
        this.languageService = languageService;
    }

    public void validateServer(CollectionConfig config) {
        if (!healthCheckClient.isServerAvailable(config.serverURL)) {
            throw new ServerException(String.format(
                    languageService
                            .getDescriptionByKey("Item.collectionUnavailableServerNotResponding"),
                    config.title,
                    config.serverURL
            ));
        }
    }
}
