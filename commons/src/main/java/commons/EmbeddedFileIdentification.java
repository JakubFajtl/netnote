package commons;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;
import java.net.URI;
import java.util.Objects;

@MappedSuperclass
public class EmbeddedFileIdentification implements Serializable {

    @EmbeddedId
    public FileKey fileKey;

    public String contentType;

    public EmbeddedFileIdentification() {
        this.fileKey = new FileKey(0L, "untitled");
    }

    public EmbeddedFileIdentification(EmbeddedFile file){
        this.fileKey = file.fileKey;
        this.contentType = file.contentType;
    }

    public EmbeddedFileIdentification(String fileName, String contentType, long noteId) {
        this.fileKey = new FileKey(noteId, fileName);
        this.contentType = contentType;
    }

    public FileKey getFileKey() {
        return fileKey;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFilePath(CollectionConfig config) {
        try {
            String encodedCollectionKey = new URI(null, null,
                    config.collectionKey, null).toASCIIString();
            String encodedFileName = new URI(null, null,
                    getFileKey().getFilename(), null).toASCIIString();
            return (config.serverURL.endsWith("/") ? config.serverURL : config.serverURL + "/")
                    + "api/files/" + encodedCollectionKey + "/"
                    + fileKey.getNoteId() + "/" + encodedFileName;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setFileKey(long noteId, String fileName) {
        this.fileKey.setNoteId(noteId);
        this.fileKey.setFilename(fileName);
    }

    public void setName(String newName) {
        this.fileKey.setFilename(newName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmbeddedFileIdentification that = (EmbeddedFileIdentification) o;
        return Objects.equals(fileKey, that.fileKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileKey);
    }

    @Override
    public String toString() {
        return fileKey.getFilename();
    }
}
