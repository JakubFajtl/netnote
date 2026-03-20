package client.utils;

import commons.CollectionConfig;
import commons.EmbeddedFileIdentification;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.List;

public interface FilesClient {
    void uploadFileToNote(File file, long noteId, CollectionConfig config);
    List<EmbeddedFileIdentification> getAllFilesFromNote(long noteId, CollectionConfig config);
    void deleteFile(long noteId, String fileName, CollectionConfig config);
    void renameFile(long noteId, String oldName, String newName, CollectionConfig config);
    Resource getFileContentByName(long noteId, String fileName, CollectionConfig config);
}
