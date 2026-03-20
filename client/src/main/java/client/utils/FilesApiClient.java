package client.utils;

import client.exceptions.ResponseValidator;
import client.exceptions.ServerValidator;
import client.services.LanguageService;
import commons.CollectionConfig;
import commons.EmbeddedFileIdentification;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.net.URI;
import java.util.List;

public class FilesApiClient implements FilesClient{
    private final ResponseValidator responseValidator;
    private final ServerValidator serverValidator;
    private final LanguageService languageService;

    @Inject
    public FilesApiClient(
            ResponseValidator responseValidator,
            ServerValidator serverValidator, LanguageService languageService) {
        this.responseValidator = responseValidator;
        this.serverValidator = serverValidator;
        this.languageService = languageService;
    }

    /**
     *
     * @param file uploads given file
     * @param noteId to the given note
     * @param config the config of note collection
     */
    public void uploadFileToNote(File file, long noteId, CollectionConfig config) {
        serverValidator.validateServer(config);
        try (var client = ClientBuilder.newClient(new ClientConfig())) {

            // Create a FormDataMultiPart instance
            FormDataMultiPart multiPart = new FormDataMultiPart();
            FileDataBodyPart filePart = new FileDataBodyPart("file", file);
            multiPart.bodyPart(filePart);

            Response response = client.target(config.serverURL)
                    .path("api/files/" + noteId)
                    .request()
                    .post(Entity.entity(multiPart, multiPart.getMediaType()));

            multiPart.close();
            responseValidator.validateResponse(response);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Resource getFileContentByName(long noteId, String fileName, CollectionConfig config) {
        serverValidator.validateServer(config);
        try (var client = ClientBuilder.newClient(new ClientConfig())) {
            String encodedName = new URI(null, null,
                    fileName, null).toASCIIString();
            String encodedCollectionKey = new URI(null, null,
                    config.collectionKey, null).toASCIIString();

            Response response = client
                    .target(config.serverURL)
                    .path("api/files/" + encodedCollectionKey + "/" + noteId + "/" + encodedName)
                    .request()
                    .get();

            responseValidator.validateResponse(response);
            byte[] fileBytes = response.readEntity(byte[].class);
            return new ByteArrayResource(fileBytes);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param noteId from a given note
     * @param config the config of note collection
     * @return a list of all files
     */
    public List<EmbeddedFileIdentification> getAllFilesFromNote(
            long noteId,
            CollectionConfig config) {
        serverValidator.validateServer(config);
        try (var client = ClientBuilder.newClient(new ClientConfig())) {
            Response response = client
                    .target(config.serverURL)
                    .path("api/files/" + noteId)
                    .request()
                    .get();
            responseValidator.validateResponse(response);

            byte[] serializedList = response.readEntity(byte[].class);
            ByteArrayInputStream byteStream = new ByteArrayInputStream(serializedList);
            ObjectInputStream objectStream = new ObjectInputStream(byteStream);
            return (List<EmbeddedFileIdentification>) objectStream
            .readObject();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteFile(long noteId, String fileName, CollectionConfig config) {
        serverValidator.validateServer(config);
        try (var client = ClientBuilder.newClient(new ClientConfig())) {
            String encodedName = new URI(null, null, fileName, null).toASCIIString();
            Response response = client
                    .target(config.serverURL)
                    .path("api/files/" + noteId + "/" + encodedName)
                    .request()
                    .delete();

            if (response.getStatus() == 404) {
                throw new RuntimeException(languageService
                        .getDescriptionByKey("Item.fileNotFoundOnServer")
                        + fileName + " (Note ID: " + noteId + ")");
            } else if (response.getStatus() >= 400) {
                throw new RuntimeException(languageService
                        .getDescriptionByKey("Item.failedToDeleteFile") +
                        languageService
                                .getDescriptionByKey("Item.serverReturnedstatus")
                        + response.getStatus());
            }

            responseValidator.validateResponse(response);
        } catch (Exception e) {
            throw new RuntimeException(languageService
                    .getDescriptionByKey("Item.unexpectedErrorDeletingFile")
                    + fileName, e);
        }
    }

    public void renameFile(long noteId, String oldName, String newName, CollectionConfig config) {
        serverValidator.validateServer(config);
        try (var client = ClientBuilder.newClient(new ClientConfig())) {

            //Using encoding on files with spaces or different signs does
            // not work had to use the oldName by itself
            String encodedOldName = new URI(null, null, oldName, null).toASCIIString();
            String encodedNewName = new URI(null, null, newName, null).toASCIIString();
            String renameUrl = config.serverURL + "api/files/" + noteId + "/" +
                    encodedOldName;

            Response response = client.target(renameUrl).queryParam(
                    "newName", encodedNewName).request().
                    put(Entity.text(""));

            if (response.getStatus() == 404) {
                throw new RuntimeException(languageService
                        .getDescriptionByKey("Item.fileNotFoundOnServer") +
                        oldName + " (Note ID: " + noteId + ")");
            } else if (response.getStatus() == 400) {
                throw new RuntimeException(languageService
                        .getDescriptionByKey("Item.fileNameAlreadyEmbedded"));
            }

            responseValidator.validateResponse(response);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(languageService
                    .getDescriptionByKey("Item.unexpectedErrorRenamingFile")
                    + oldName, e);
        }
    }


}


