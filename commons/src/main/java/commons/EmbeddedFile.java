package commons;

import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Entity
public class EmbeddedFile extends EmbeddedFileIdentification {

    @Lob
    public byte[] file;

    public EmbeddedFile(String fileName, String contentType,
                        InputStream inputStream, long noteId) throws IOException {
        super(fileName, contentType, noteId);
        try (inputStream) {
            this.file = inputStream.readAllBytes();
        }
    }

    public EmbeddedFile() {
        super();
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmbeddedFile)) return false;
        if (!super.equals(o)) return false;
        EmbeddedFile that = (EmbeddedFile) o;
        return Objects.equals(file, that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), file);
    }
}
