package client.services;

import client.utils.FilesClient;
import client.utils.NotesClient;
import commons.CollectionConfig;
import commons.Note;
import jakarta.inject.Inject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class NotesMigrationService {
    private final NotesClient notesClient;
    private final FilesClient filesClient;

    @Inject
    public NotesMigrationService(
            NotesClient notesClient,
            FilesClient filesClient) {
        this.notesClient = notesClient;
        this.filesClient = filesClient;
    }

    public void migrateNote(
            Note note,
            CollectionConfig currentConfig,
            CollectionConfig newConfig) {
        // If both collections are on the same server we simply update the note
        if (newConfig == null ||
                currentConfig.serverURL.equals(newConfig.serverURL)) {
            notesClient.updateNote(note, currentConfig, newConfig);
            return;
        }

        // If the collections are on different servers, we have to create new note on the new server
        Note noteAtNewServer = notesClient.createNote(note.title, newConfig);
        noteAtNewServer.content = note.content;

        // We update the content of the new note
        notesClient.updateNote(noteAtNewServer, newConfig, null);

        // We get all files from the note (we need to transfer them to the new service)
        var files = filesClient.getAllFilesFromNote(note.id, currentConfig);

        for (var file : files) {
            // We get the contents of the file
            var fileContent = filesClient.getFileContentByName(
                    note.id, file.fileKey.getFilename(), currentConfig);

            // We write the contents to the File object
            File fileObject = new File(file.fileKey.getFilename());
            try (FileOutputStream fos = new FileOutputStream(fileObject)) {
                fos.write(fileContent.getContentAsByteArray());
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

            // We upload the File to the new note
            filesClient.uploadFileToNote(fileObject, noteAtNewServer.id, newConfig);
        }

        // Lastly when all information is transferred, we can safely delete the old note
        notesClient.deleteNote(note.id, currentConfig);
    }
}
