package client.services;

import client.InformationalDialogue;
import client.enums.CollectionStatus;
import client.utils.CollectionClient;
import client.utils.HealthCheckClient;
import commons.CollectionConfig;
import commons.constants.AppConstants;
import jakarta.inject.Inject;
import javafx.scene.control.Alert;

import java.util.*;

public class EditCollectionsService {

    private final CollectionClient collectionClient;
    private final HealthCheckClient availabilityClient;
    private final ConfigService configService;
    private final InformationalDialogue informationalDialogue;
    private final LanguageService languageService;

    @Inject
    public EditCollectionsService(
            CollectionClient collectionClient,
            HealthCheckClient availabilityClient,
            ConfigService configService,
            InformationalDialogue informationalDialogue, LanguageService languageService) {
        this.collectionClient = collectionClient;
        this.availabilityClient = availabilityClient;
        this.configService = configService;
        this.informationalDialogue = informationalDialogue;
        this.languageService = languageService;
    }

    public boolean isValidIndex(int index) {
        return configService.isValidIndex(index);
    }

    public CollectionConfig getCollectionConfig(int index) {
        return configService.getCollectionConfig(index);
    }

    public CollectionConfig createCollectionConfigLocally(
            String collectionKey,
            String title,
            String serverURL) {
        return new CollectionConfig(collectionKey, title, serverURL);
    }

    /**
     * Checks all collections in the config file
     * and removes the ones that do not exist in the server
     */
    public void removeAllInvalidConfigs() {
        List<CollectionConfig> invalidConfigs = new ArrayList<>();
        for (CollectionConfig config : configService.getAllCollectionConfigs()) {
            CollectionStatus status = checkCollectionConfigStatus(config);
            if (status.equals(CollectionStatus.ERROR_DOES_NOT_EXIST)) {
                invalidConfigs.add(config);
            }
        }

        for (CollectionConfig config : invalidConfigs) {
            configService.forceRemoveCollectionConfig(config);
        }
    }

    /**
     * Returns the status of a collection config
     * @param config the config to check
     * @return the status of the config
     */
    public CollectionStatus checkCollectionConfigStatus(CollectionConfig config) {
        // check for null or empty fields
        if (config.collectionKey == null || config.title == null || config.serverURL == null ||
                config.collectionKey.isEmpty() || config.title.isEmpty() ||
                config.serverURL.isEmpty()) {
            return CollectionStatus.EMPTY;
        }

        // check if the title is unique
        if (!isTitleUnique(config)) {
            return CollectionStatus.TITLE_NOT_UNIQUE;
        }

        // check if server is available
        if (!availabilityClient.isServerAvailable(config.serverURL)) {
            return CollectionStatus.SERVER_NOT_FOUND;
        }

        return checkValidCollectionConfigStatus(config);
    }

    /**
     * Returns the status of the collection config assuming it is valid
     * @param config the config to check
     * @return the status of the config
     */
    public CollectionStatus checkValidCollectionConfigStatus(CollectionConfig config) {
        // check if the collection exists in the server
        boolean existsRemotely =
                collectionClient.checkIfCollectionExistsRemotely(config);

        // check if the collections exists locally
        boolean existsLocally =
                checkIfCollectionExistsLocally(config);

        if (existsLocally && existsRemotely) {
            if (isCollectionUpToDate(config)) {
                return CollectionStatus.EXISTS_UP_TO_DATE;
            }
            else {
                return CollectionStatus.EXISTS_NOT_UP_TO_DATE;
            }
        }
        else if (!existsLocally && existsRemotely) {
            return CollectionStatus.EXISTS_REMOTELY;
        }
        else if (!existsLocally) {
            return CollectionStatus.DOES_NOT_EXIST;
        }
        else {
            return CollectionStatus.ERROR_DOES_NOT_EXIST;
        }
    }

    /**
     * If the key isn't present it creates a new configuration
     * if it is present it renames the existing one
     * if the key is not on the server it creates a new one on the server
     * @param currentConfig the config of a collection to be saved
     * @return Status of how the operation went
     */
    public CollectionStatus saveCollectionConfig(CollectionConfig currentConfig) {
        CollectionStatus status = checkCollectionConfigStatus(currentConfig);

        if (!status.isSuccess() || status.isUpToDate()) {
            return status;
        }

        switch (status) {
            // if a collection exists then it is updated
            case CollectionStatus.EXISTS_NOT_UP_TO_DATE -> {
                configService.fetchCollectionConfig(currentConfig);
                return CollectionStatus.UPDATED;
            }
            // if a collection exists only on the server then it is added locally
            case CollectionStatus.EXISTS_REMOTELY -> {
                configService.addCollectionConfig(currentConfig);
                return CollectionStatus.ADDED;
            }
            // if a collection does not exist then it is added both on the server and locally
            case CollectionStatus.DOES_NOT_EXIST -> {
                collectionClient.createCollection(currentConfig);
                configService.addCollectionConfig(currentConfig);
                return CollectionStatus.CREATED;
            }
            // This exception should never be thrown, because status.isSuccess() should be false
            default -> throw new IllegalArgumentException();
        }
    }

    /**
     * Sets the collection as default
     * @param index the index of the collection to set
     */
    public void setCollectionAsDefault(int index) {
        if (!isValidIndex(index))
            return;
        configService.setDefaultCollectionKey(getCollectionConfig(index).collectionKey);
    }

    /**
     * Returns true iff the title of the config is unique (locally)
     * @param config the config to check
     * @return true iff the title is unique
     */
    public boolean isTitleUnique(CollectionConfig config) {
        return configService.getAllCollectionConfigs()
                .stream().noneMatch(c -> Objects.equals(c.title, config.title) &&
                        !Objects.equals(c.collectionKey, config.collectionKey));
    }

    public Optional<String> getTitleOfCollection(String collectionKey) {
        return configService.getTitleOfCollectionConfig(collectionKey);
    }

    public List<String> getListOfCollectionTitles() {
        createDefaultCollectionIfEmpty();
        return configService.getAllCollectionConfigs()
                .stream()
                .filter(n -> n.title != null)
                .map(n -> n.title)
                .toList();
    }

    public boolean isCollectionUpToDate(CollectionConfig config) {
        return configService.getAllCollectionConfigs()
                .stream()
                .anyMatch(c -> c.equals(config));
    }

    public boolean checkIfCollectionExistsLocally(CollectionConfig config) {
        return configService.getAllCollectionConfigs()
                .stream()
                .anyMatch(
                        c -> c.collectionKey.trim().equals(config.collectionKey.trim()) &&
                                c.serverURL.trim().equals(config.serverURL.trim()));
    }

    public int getIndexOfDefaultConfigKey() {
        var optionalKey = getIndexOfConfigKey(configService.getDefaultCollectionKey());
        if (optionalKey.isEmpty()) {
            createDefaultCollectionIfEmpty();

            // If there is no default collection, the first one is marked as default
            configService.setDefaultCollectionKey(
                    configService.getCollectionConfig(0).collectionKey
            );

            return 0;
        }

        return optionalKey.getAsInt();
    }

    public void createDefaultCollectionIfEmpty() {
        if (configService.getAllCollectionConfigs().isEmpty()) {
            // If there are no collections, the default one is created
            String serverUrl = AppConstants.DefaultServerUrl;
            while (!availabilityClient.isServerAvailable(serverUrl)) {
                var alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(
                        languageService.getDescriptionByKey("Item.errorConnectingToServer"));
                alert.setContentText(languageService.getDescriptionByKey("Item.serverAt")
                        + serverUrl
                        + languageService.getDescriptionByKey("Item.isUnavailable"));
                alert.showAndWait();
                serverUrl = informationalDialogue.showFirstCollectionServerUrlDialogue();
            }

            saveCollectionConfig(new CollectionConfig(
                    AppConstants.DefaultCollectionStartingNameServer,
                    AppConstants.DefaultCollectionStartingNameClient,
                    serverUrl));

            configService.setDefaultCollectionKey(AppConstants.DefaultCollectionStartingNameServer);
        }
    }

    public OptionalInt getIndexOfConfigKey(String configKey) {
        return configService.getIndexOfConfigKey(configKey);
    }

    public void removeCollectionConfigLocally(int index) {
        configService.removeCollectionConfig(getCollectionConfig(index));
    }

    public void forceRemoveCollectionConfigLocally(CollectionConfig config) {
        configService.forceRemoveCollectionConfig(config);
    }

    public void removeCollectionRemotely(CollectionConfig config) {
        collectionClient.deleteCollection(config);
    }
}
