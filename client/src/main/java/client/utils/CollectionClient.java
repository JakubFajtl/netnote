package client.utils;

import commons.Collection;
import commons.CollectionConfig;

public interface CollectionClient {
    Collection createCollection(CollectionConfig config);
    boolean checkIfCollectionExistsRemotely(CollectionConfig config);
    void deleteCollection(CollectionConfig config);
}
