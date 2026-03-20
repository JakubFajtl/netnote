package client.services;

import client.data.ConfigFileData;
import client.exceptions.ConfigException;
import client.exceptions.DeleteDefaultException;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.CollectionConfig;
import commons.constants.AppConstants;
import jakarta.inject.Inject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

// This service is responsible for storing a config
// It is used by EditCollectionsService
public class ConfigService {

    private ConfigFileData configData;
    private final String configFileName;
    private boolean useMemory = false;
    private final LanguageService languageService;

    @Inject
    public ConfigService(String configFileId, LanguageService languageService) {
        this.languageService = languageService;
        // When testing, we don't want to read/write to config files
        if (configFileId != null && configFileId.equals("Test")) {
            useMemory = true;
        }
        if (configFileId == null || configFileId.isEmpty()) {
            // If the user didn't provide any configFileId
            // then the default config file name is used
            configFileName = AppConstants.DefaultConfigFileName + ".json";
        }
        else {
            configFileName = AppConstants.DefaultConfigFileName + "_" + configFileId + ".json";
        }

        readConfigFile();
    }

    public void initializeCollectionConfigData() {
        configData.collectionConfigs = new ArrayList<>();
        saveConfigData();
    }

    public void clearAllConfigData() {
        configData = new ConfigFileData();
        initializeCollectionConfigData();
    }

    /**
     * Reads the config file and saves the data in the configData field
     */
    private void readConfigFile() {
        if (useMemory) {
            clearAllConfigData();
            return;
        }
        File file = new File(configFileName);
        if (!file.exists()) {
            clearAllConfigData();
        }
        try (var reader = new FileReader(file)) {
            configData = new ObjectMapper().readValue(reader, ConfigFileData.class);

            if (configData == null) {
                clearAllConfigData();
            }
        }
        catch (IOException e) {
            clearAllConfigData();
            saveConfigData();
        }
    }

    /**
     * Writes the current data to the config file
     */
    private void saveConfigData() {
        configData.collectionConfigs.sort(null);
        if (useMemory) return;
        try (var writer = new FileWriter(configFileName)) {
            new ObjectMapper().writeValue(writer, configData);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setLocale(Locale locale) {
        configData.language = locale;
        saveConfigData();
    }

    public Locale getLocale(){
        return configData.language != null ? configData.language : Locale.ENGLISH;
    }

    public String getDefaultCollectionKey() {
        return configData.defaultCollectionKey;
    }

    public void setDefaultCollectionKey(String defaultCollectionKey) {
        if (configData.collectionConfigs
                .stream().filter(c -> c.collectionKey.equals(defaultCollectionKey))
                .findFirst().isEmpty()) {
            throw new ConfigException(languageService.getDescriptionByKey("Item.collectionWithKey")
                    + defaultCollectionKey
                    + languageService.getDescriptionByKey("Item.doesNotExist"));
        }
        configData.defaultCollectionKey = defaultCollectionKey;
        saveConfigData();
    }

    public OptionalInt getIndexOfConfigKey(String configKey) {
        return IntStream.range(0, configData.getCollectionConfigs().size())
                .filter(i -> configData.getCollectionConfigs()
                        .get(i).collectionKey.equals(configKey))
                .findFirst();
    }

    public Optional<String> getTitleOfCollectionConfig(String configKey) {
        var index = getIndexOfConfigKey(configKey);
        if (index.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(configData.collectionConfigs
                .get(index.getAsInt()).title);

    }

    public CollectionConfig getCollectionConfig(String collectionKey) {
        var config = configData.collectionConfigs.stream()
                .filter(c -> c.collectionKey.equals(collectionKey)).findFirst();

        if (config.isEmpty()) {
            throw new ConfigException(
                    languageService.getDescriptionByKey("Item.collectionDoesNotExist"));
        }

        return config.get();
    }

    public CollectionConfig getCollectionConfig(int index) {
        if (!isValidIndex(index)) {
            throw new ConfigException(languageService.getDescriptionByKey("Item.theIndex")
                    + index
                    + languageService.getDescriptionByKey("Item.isNotvalid"));
        }

        return configData.getCollectionConfigs().get(index);
    }

    public boolean isValidIndex(int index) {
        return index >= 0 && index < configData.getCollectionConfigs().size();
    }

    public List<CollectionConfig> getAllCollectionConfigs() {
        return configData.getCollectionConfigs();
    }

    public void addCollectionConfig(CollectionConfig config) {
        if (getIndexOfConfigKey(config.collectionKey).isPresent()) {
            throw new ConfigException(
                    languageService.getDescriptionByKey("Item.collectionWithKey")
                            + config.collectionKey
                            + languageService.getDescriptionByKey("Item.alreadyExists"));
        }

        configData.getCollectionConfigs().add(config);
        saveConfigData();
    }

    public void fetchCollectionConfig(CollectionConfig config) {
        var index = getIndexOfConfigKey(config.collectionKey);

        if (index.isEmpty()) {
            throw new ConfigException(
                    languageService.getDescriptionByKey("Item.collectionWithKey")
                            + config.collectionKey
                            + languageService.getDescriptionByKey("Item.doesNotExist"));
        }

        configData.getCollectionConfigs().set(index.getAsInt(), config);
        saveConfigData();
    }

    public void removeCollectionConfig(CollectionConfig config) {
        if (config.collectionKey.equals(configData.defaultCollectionKey)) {
            throw new DeleteDefaultException(
                    languageService.getDescriptionByKey("Item.setOtherDefaultCollection"));
        }

        forceRemoveCollectionConfig(config);
    }

    /**
     * The force removal removes the collection even if it is the default one
     * @param config the config to remove
     */
    public void forceRemoveCollectionConfig(CollectionConfig config) {
        var index = getIndexOfConfigKey(config.collectionKey);

        if (index.isEmpty()) {
            throw new ConfigException(
                    languageService.getDescriptionByKey("Item.collectionWithKey")
                            + config.collectionKey
                            + languageService.getDescriptionByKey("Item.doesNotExist"));
        }

        configData.getCollectionConfigs().remove(index.getAsInt());
        saveConfigData();
    }
}
