package client.utils;

import commons.CollectionConfig;
import commons.Note;
import commons.NoteIdentification;

import java.util.List;

public interface NotesClient {
    Note pullNote(long noteId, CollectionConfig config);
    List<NoteIdentification> getAllNoteInfos(CollectionConfig config);
    List<NoteIdentification> getFilteredNoteInfos(String query, CollectionConfig config);
    Note createNote(String title, CollectionConfig config);
    void updateNote(Note note, CollectionConfig config, CollectionConfig newConfig);
    void deleteNote(long noteId, CollectionConfig config);
}
