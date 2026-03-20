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

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import client.exceptions.ResponseValidator;
import client.exceptions.ServerValidator;
import commons.CollectionConfig;
import commons.Note;
import commons.NoteIdentification;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;

public class NotesApiClient implements NotesClient {
    private final ResponseValidator responseValidator;
    private final ServerValidator serverValidator;

    @Inject
    public NotesApiClient(
            ResponseValidator responseValidator,
            ServerValidator serverValidator) {
        this.responseValidator = responseValidator;
        this.serverValidator = serverValidator;
    }

    /**
     * @param noteId id of the note we want to get from the server
     * @return the note from the server by the corresponding id
     */
    public Note pullNote(long noteId, CollectionConfig config) {
        serverValidator.validateServer(config);
        try (var client = ClientBuilder.newClient(new ClientConfig())) {
            Response response = client
                    .target(config.serverURL)
                    .path("api/notes/" + config.collectionKey + "/" + noteId)
                    .request(APPLICATION_JSON)
                    .get();
            responseValidator.validateResponse(response);
            return response.readEntity(Note.class);
        }
    }

    /**
     * REST API request to get info of all notes from the server
     *
     * @return the list of notes
     */
    public List<NoteIdentification> getAllNoteInfos(CollectionConfig config) {
        return getFilteredNoteInfos(null, config);
    }

    /**
     * REST API request to get all or filtered notes' info from server
     * @param query string with query
     * @return all notes' info if query is null, filtered notes' info otherwise
     */
    public List<NoteIdentification> getFilteredNoteInfos(
            String query,
            CollectionConfig config) {
        serverValidator.validateServer(config);
        try (var client = ClientBuilder.newClient(new ClientConfig())) {
            WebTarget target = client.target(config.serverURL)
                    .path("api/notes/" + config.collectionKey);
            if (query != null && !query.isEmpty()) {
                target = target.queryParam("query", query);
            }
            Response response = target.request(APPLICATION_JSON).get(new GenericType<>() {});
            responseValidator.validateResponse(response);
            return response.readEntity(new GenericType<>() {});
        }
    }

    /**
     * REST API request to create a note in the server
     *
     * @return the note created by the server
	 * */
    public Note createNote(String title, CollectionConfig config) {
        serverValidator.validateServer(config);
        try (var client = ClientBuilder.newClient(new ClientConfig())) {
            Response response = client
                    .target(config.serverURL)
					.path("api/notes/" + config.collectionKey + "/" + title)
                    .request(APPLICATION_JSON)
					.post(Entity.json(null));
            responseValidator.validateResponse(response);
            return response.readEntity(new GenericType<>() {});
        }
    }

    /**
     * REST API request to update the content of the note in the server
     * This method assumes that both old and new configs are on the same server
     */
    public void updateNote(Note note, CollectionConfig config, CollectionConfig newConfig) {
        serverValidator.validateServer(config);
        if (newConfig != null)
            serverValidator.validateServer(newConfig);

        if (newConfig != null &&
                !config.serverURL.equals(newConfig.serverURL)) {
            return;
        }

        try (var client = ClientBuilder.newClient(new ClientConfig())) {
            Response response = client
                    .target(config.serverURL).path(
                            "api/notes/" + config.collectionKey + "/" + note.id)
                    .queryParam("newCollectionKey",
                            newConfig == null ? null : newConfig.collectionKey)
                    .request(APPLICATION_JSON)
                    .put(Entity.json(note));
            responseValidator.validateResponse(response);
        }
    }

    /**
     * REST API request to delete the note from the server
     * @param noteId the id of the note to delete
     * @param config the config of note collection
     */
    public void deleteNote(long noteId, CollectionConfig config) {
        serverValidator.validateServer(config);
        try (var client = ClientBuilder.newClient(new ClientConfig())) {
            Response response = client
                    .target(config.serverURL).path(
                            "api/notes/" + config.collectionKey + "/" + noteId)
                    .request(APPLICATION_JSON)
                    .delete();
            responseValidator.validateResponse(response);
        }
    }
}