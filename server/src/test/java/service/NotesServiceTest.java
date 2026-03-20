package service;

import commons.Collection;
import commons.Note;
import commons.NoteIdentification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server.database.NotesRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NotesServiceTest {

    @Mock
    private NotesRepository notesRepository;

    @Mock
    private CollectionService collectionService;

    private NotesService notesService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        notesService = new NotesService(notesRepository, collectionService);
    }

    @Test
    void testCreateNote_Success() {
        Note note = new Note();
        String collectionKey = "collection key";
        Collection collection = new Collection(collectionKey);
        note.collection = collection;
        when(notesRepository.save(any(Note.class))).thenReturn(note);
        when(collectionService.getCollection(collectionKey)).thenReturn(Optional.of(collection));

        var createdNote = notesService.createNote(collectionKey, "hello");

        assertTrue(createdNote.isPresent());
        assertEquals(collection, createdNote.get().collection);
        verify(notesRepository).save(any(Note.class));
    }

    @Test
    void testGetNoteById_Success() {
        Collection collection = new Collection("collection key");
        Note note = new Note("Test Title", collection, "Test Content");
        when(notesRepository.findById(1L)).thenReturn(Optional.of(note));

        Optional<Note> result = notesService.getNoteById(1L, collection.collectionKey);

        assertTrue(result.isPresent());
        assertEquals("Test Title", result.get().title);
        verify(notesRepository).findById(1L);
    }

    @Test
    void testGetNoteById_NoteNotFound() {
        Collection collection = new Collection("collection-key");
        Note note = new Note("title", collection, "content");
        when(notesRepository.findById(1L)).thenReturn(Optional.empty());
        when(notesRepository.findByCollection(collection)).thenReturn(List.of(note));

        Optional<Note> result = notesService.getNoteById(1L, collection.collectionKey);

        assertFalse(result.isPresent());
        verify(notesRepository).findById(1L);
    }

    @Test
    void testDeleteNoteById_Success() {
        Collection collection = new Collection("collection-key");
        Note note = new Note("title", collection, "content");
        when(notesRepository.findById(1L)).thenReturn(Optional.of(note));
        when(notesRepository.findByCollection(collection)).thenReturn(List.of(note));

        Optional<Note> deletedNote = notesService.deleteNoteById(1L, collection.collectionKey);

        assertTrue(deletedNote.isPresent());
        verify(notesRepository).delete(note);
    }

    @Test
    void testDeleteNoteById_NotFound() {
        Collection collection = new Collection("collection-key");
        when(notesRepository.findById(1L)).thenReturn(Optional.empty());
        when(notesRepository.findByCollection(collection)).thenReturn(List.of());

        Optional<Note> deletedNote = notesService.deleteNoteById(1L, collection.collectionKey);

        assertFalse(deletedNote.isPresent());
        verify(notesRepository, never()).delete(any());
    }

    @Test
    void testGetNotesWithQuery() {
        Collection collection1 = new Collection("collection key 1");
        Collection collection2 = new Collection("collection key 2");
        List<Note> notes = List.of(
                new Note("Title 1", collection1, "Content 1"),
                new Note("Title 2", collection1, "Content 2"),
                new Note("Title 3", collection2, "Content 3"),
                new Note("Title 4", collection2, "Content 4")
        );

        when(notesRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase("query", "query"))
                .thenReturn(List.of(notes.get(1), notes.get(2)));

        when(notesRepository.findByCollection(collection1))
                .thenReturn(List.of(notes.get(0), notes.get(1)));

        List<NoteIdentification> result = notesService.getNotes("query", collection1.collectionKey);

        assertEquals(
                List.of(new NoteIdentification(notes.get(1))),
                result);
        verify(notesRepository).findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase("query", "query");
        verify(notesRepository).findByCollection(collection1);
    }
}