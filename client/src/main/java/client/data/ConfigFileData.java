package client.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import commons.CollectionConfig;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ConfigFileData {
    // More config data can be added here later
    public List<CollectionConfig> collectionConfigs;
    public String defaultCollectionKey;
    public Locale language;

    @JsonCreator
    public ConfigFileData(@JsonProperty("collectionConfigs")
                               List<CollectionConfig> collectionConfigs,
                          @JsonProperty("defaultCollectionKey")
                                String defaultCollectionKey,
                          @JsonProperty("language")
                              Locale language) {
        this.collectionConfigs = collectionConfigs;
        this.defaultCollectionKey = defaultCollectionKey;
        this.language = language;
    }

    public ConfigFileData() {}

    public List<CollectionConfig> getCollectionConfigs() {
        return collectionConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigFileData that = (ConfigFileData) o;
        return Objects.equals(collectionConfigs, that.collectionConfigs) &&
                Objects.equals(defaultCollectionKey, that.defaultCollectionKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collectionConfigs, defaultCollectionKey);
    }
}
