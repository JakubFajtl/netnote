package service;

import commons.EmbeddedFile;
import commons.EmbeddedFileIdentification;
import commons.FileKey;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import server.ErrorResponseBuilder;
import server.database.FilesRepository;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FilesService {

    private final FilesRepository repo;
    ErrorResponseBuilder builder;

    public FilesService (FilesRepository repo, ErrorResponseBuilder builder) {
        this.repo = repo;
        this.builder = builder;
    }

    /**
     *
     * @param fileName what name to give the file
     * @param file to upload
     * @param notid to which note to upload
     * @return created file
     * @throws IOException
     */

    public EmbeddedFile createFile(String fileName,
                                   String contentType,
                                   InputStream file,
                                   long notid) throws IOException {
        return repo.save(new EmbeddedFile(fileName, contentType, file, notid));
    }

    public Optional<EmbeddedFile> getFileById(FileKey id){
        return repo.findById(id);
    }
    public Optional<EmbeddedFile> updateFileName(FileKey id, String newName){
        return repo.findById(id).map(file -> {
            file.setName(newName);
            return repo.save(file);
        });
    }

    /**
     *
     * @param notid note ID to get files from
     * @return queries the database for all files associated with a given notid
     */

    public List<EmbeddedFile> getFiles(long notid) {
        List<EmbeddedFile> files = repo.findByFileKeyNotid(notid);
        return new ArrayList<>(files);
    }
    public List<EmbeddedFileIdentification> getFileIdentifications(long notid) {
        List<EmbeddedFile> files = repo.findByFileKeyNotid(notid);
        List<EmbeddedFileIdentification> fileIdentifications = new ArrayList<>();
        for(EmbeddedFile item : files){
            fileIdentifications.add(new EmbeddedFileIdentification(item));
            //I don't know why, but functional programming didn't work, so I did it the OG way
        }
        return fileIdentifications;
    }

    /**
     *
     * @param returnValue object that needs to be turned into a byte array
     * @return byte Array
     * @throws IOException
     */

    public ResponseEntity<byte[]> buildResponseEntityByteArray(Object returnValue)
            throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
        objectStream.writeObject(returnValue);
        objectStream.flush();
        ResponseEntity<byte[]> responseStream = (Optional.of(byteStream
                .toByteArray()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    return builder.buildNotFound(
                            "Item.fileDoesNotExist");
                });
        return responseStream;
    }

    /**
     * Delete all files associated with a particular note
     *
     * @param notid
     * @return
     */
    public Optional<List<EmbeddedFile>> deleteNoteFiles(long notid) {
        List<EmbeddedFile> files = getFiles(notid);
        repo.deleteAll(files);
        return Optional.of(files);
    }

    /**
     * Delete a file from a note
     * @param noteId - note id
     * @param fileName - name of file
     * @throws FileNotFoundException - cannot find file exception
     */
    public void deleteFile(long noteId, String fileName) throws FileNotFoundException {
        List<EmbeddedFile> files = repo.findByFileKeyNotid(noteId);

        Optional<EmbeddedFile> fileOptional = files.stream()
                .filter(file -> file.getFileKey().getFilename().equals(fileName))
                .findFirst();

        if (fileOptional.isEmpty()) {
            throw new FileNotFoundException("Item.fileNotFound");
        }

        EmbeddedFile file = fileOptional.get();
        repo.delete(file);

    }

    public void renameFile(long noteId, String oldName, String newName)
            throws FileNotFoundException {

        Optional<EmbeddedFile> fileOptional = repo.findByFileKeyNotid(noteId)
                .stream()
                .filter(file -> file.getFileKey().getFilename().equals(oldName))
                .findFirst();

        if (fileOptional.isEmpty()) {
            throw new FileNotFoundException("Item.fileNotFound");
        }

        EmbeddedFile file = fileOptional.get();

        // Check if the new name is unique
        boolean isNameTaken = repo.findByFileKeyNotid(noteId)
                .stream()
                .anyMatch(existingFile -> existingFile.getFileKey().getFilename().equals(newName));

        if (isNameTaken) {
            throw new IllegalArgumentException("Item.fileNameAlreadyExists");
        }

        // Delete the old file. Had to do it this way,
        // since just setting the FileKey with the new name did not work...
        repo.delete(file);

        // Save the file with the new name
        file.setFileKey(noteId, newName);
        repo.save(file);
    }

}
