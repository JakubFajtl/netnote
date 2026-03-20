/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.utils;

import client.exceptions.ResponseValidator;
import client.exceptions.ServerValidator;
import commons.Collection;
import commons.CollectionConfig;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

public class CollectionApiClient implements CollectionClient {

    private final ResponseValidator responseValidator;
    private final ServerValidator serverValidator;

    @Inject
    public CollectionApiClient(
            ResponseValidator responseValidator,
            ServerValidator serverValidator) {
        this.responseValidator = responseValidator;
        this.serverValidator = serverValidator;
    }

    /**
     * Creates a collection on the server
     * @param config the config to create
     * @return the created collection from the server
     */
    @Override
    public Collection createCollection(CollectionConfig config) {
        serverValidator.validateServer(config);
        try (var client = ClientBuilder.newClient(new ClientConfig())) {
            Response response = client
                    .target(config.serverURL)
                    .path("api/collections/" + config.collectionKey)
                    .request(APPLICATION_JSON)
                    .post(Entity.json(null));
            responseValidator.validateResponse(response);
            return response.readEntity(new GenericType<>() {});
        }
    }

    /**
     * Checks if a collection is on the server
     * @param config the config to check
     * @return true iff a collection is on the server
     */
    @Override
    public boolean checkIfCollectionExistsRemotely(CollectionConfig config) {
        serverValidator.validateServer(config);
        try (var client = ClientBuilder.newClient(new ClientConfig())) {
            Response response = client
                    .target(config.serverURL)
                    .path("api/collections/" + config.collectionKey + "/exists")
                    .request(APPLICATION_JSON)
                    .get();
            responseValidator.validateResponse(response);
            return response.readEntity(new GenericType<>() {});
        }
    }

    /**
     * Deletes a collection from the server
     * @param config the config to delete
     */
    @Override
    public void deleteCollection(CollectionConfig config) {
        serverValidator.validateServer(config);
        try (var client = ClientBuilder.newClient(new ClientConfig())) {
            Response response = client
                    .target(config.serverURL)
                    .path("api/collections/" + config.collectionKey)
                    .request(APPLICATION_JSON)
                    .delete();
            responseValidator.validateResponse(response);
        }
    }
}
