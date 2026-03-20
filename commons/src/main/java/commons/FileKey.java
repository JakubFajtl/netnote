package commons;


import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

//files need a class to be identified by
@Embeddable
public class FileKey implements Serializable {
    private String fileName;


    private long notid; //it is notid instead of noteId because of how JPA treats capital letters

    public FileKey(){
    }
    public FileKey(Long noteId, String fileName){
        this.fileName = fileName;
        this.notid = noteId;
    }
    public String getFilename() {
        return fileName;
    }

    public void setFilename(String filename) {
        this.fileName = filename;
    }

    public Long getNoteId() {
        return notid;
    }

    public void setNoteId(Long noteId) {
        this.notid = noteId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileKey fileKey = (FileKey) o;
        return Objects.equals(fileName, fileKey.fileName) &&
                Objects.equals(getNoteId(), fileKey.getNoteId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, getNoteId());
    }
}
