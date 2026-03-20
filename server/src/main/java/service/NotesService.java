package service;

import commons.Collection;
import commons.Note;
import commons.NoteIdentification;
import org.springframework.stereotype.Service;
import server.database.NotesRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotesService {

    private final NotesRepository repo;
    private final CollectionService collectionService;

    // Constructor injection
    public NotesService(NotesRepository repo, CollectionService collectionService) {
        this.collectionService = collectionService;
        this.repo = repo;
    }

    public Optional<Collection> getCollection(String collectionKey) {
        return collectionService.getCollection(collectionKey);
    }

    public boolean isCollectionInvalid(String collectionKey) {
        return getCollection(collectionKey).isEmpty();
    }

    public Optional<Note> getNoteWithValidCollection(Optional<Note> note, String collectionKey) {
        if (note.isPresent() &&
                !note.get().collection.collectionKey.equals(collectionKey)) {
            return Optional.empty();
        }

        return note;
    }

    public Optional<Note> createNote(String collectionKey, String title) {
        var collection = getCollection(collectionKey);

        return Optional.of(repo.save(new Note(title, collection.get(), "")));
    }

    public Optional<Note> getNoteById(long noteId, String collectionKey) {
        // Business logic to fetch a note by ID
        return getNoteWithValidCollection(repo.findById(noteId), collectionKey);
    }

    public Optional<Note> updateNoteCollection(long noteId, String newCollectionKey) {
        return repo.findById(noteId).map(note -> {
            note.collection = new Collection(newCollectionKey);
            return repo.save(note);
        });
    }

    public Optional<Note> updateNoteTitleAndContent(
            long noteId,
            Note newNote,
            String collectionKey) {
        return getNoteWithValidCollection(
                repo.findById(noteId).map(note -> {
                    note.title = newNote.title;
                    note.content = newNote.content;
                    return repo.save(note);
                }),
            collectionKey);
    }

    public boolean isTitleUniqueInCollection(Collection collection, String title, Long noteId) {
        List<Note> notesInCollection = repo.findByCollection(collection);

        return notesInCollection.stream()
                .noneMatch(note -> note.id != noteId && note.title.equalsIgnoreCase(title));
    }


    public Optional<Note> deleteNoteById(long noteId, String collectionKey) {
        return getNoteWithValidCollection(
                repo.findById(noteId).map(note -> {
                    repo.delete(note);
                    return note;
                }),
                collectionKey);
    }

    public List<NoteIdentification> getNotes(String query, String collectionKey) {
        // Fetch notes based on the query; if query is null, fetch all notes in that collection
        List<Note> notes = repo.findByCollection(new Collection(collectionKey));
        if (query != null) {
            List<Note> filteredNotes =
                    repo.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(query, query);
            notes = notes
                    .stream()
                    .filter(filteredNotes::contains)
                    .toList();
        }

        return notes.stream()
                .map(NoteIdentification::new)
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));
    }
}