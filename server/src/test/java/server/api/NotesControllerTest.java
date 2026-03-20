package server.api;

import commons.Collection;
import commons.Note;
import commons.NoteIdentification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import service.FilesService;
import service.NotesService;
import server.ErrorResponseBuilder;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotesControllerTest {
    private final Random random = new Random();

    @Mock
    private NotesService notesService;
    @Mock
    private ErrorResponseBuilder errorResponseBuilder;

    private NotesController controller;

    private FilesService filesService;

    @BeforeEach
    void setup() {
        controller = new NotesController(notesService, errorResponseBuilder, filesService);
    }

    @Test
    void createNoteTest_Success() {
        String collectionKey = "collection key";
        Collection collection = new Collection(collectionKey);
        Note note = new Note("title", collection, "content");
        when(notesService.createNote(collectionKey, note.title)).thenReturn(Optional.of(note));

        ResponseEntity<Note> response = controller.createNote(collectionKey, note.title);

        assertEquals(note, response.getBody());
        assertEquals(200, response.getStatusCode().value());
        verify(notesService).createNote(collectionKey, note.title);
    }

    @Test
    void createNoteTest_ServerError() {
        String collectionKey = "collection key";
        RuntimeException exception = new RuntimeException("Server error");
        ResponseEntity<Object> expectedResponse = ResponseEntity.internalServerError().build();

        when(notesService.createNote(collectionKey, "hello")).thenThrow(exception);
        when(errorResponseBuilder.buildServerError(exception)).thenReturn(expectedResponse);

        ResponseEntity<Note> response = controller.createNote(collectionKey, "hello");

        assertEquals(expectedResponse, response);
        verify(notesService).createNote(collectionKey, "hello");
        verify(errorResponseBuilder).buildServerError(exception);
    }

    @Test
    void getNoteByIdTest_NotFound() {
        long noteId = random.nextLong();
        String collectionKey = "collection-key";
        when(notesService.getNoteById(noteId, collectionKey)).thenReturn(Optional.empty());
        ResponseEntity<Object> expectedResponse = ResponseEntity.notFound().build();

        when(errorResponseBuilder.buildNotFound(any()))
                .thenReturn(expectedResponse);
        ResponseEntity<Note> response = controller.getNoteById(noteId, collectionKey);

        assertEquals(expectedResponse, response);
        verify(notesService).getNoteById(noteId, collectionKey);
        verify(errorResponseBuilder).buildNotFound(any());
    }

    @Test
    void getNoteByIdTest_Success() {
        Collection collection = new Collection("collection key");
        long noteId = random.nextLong();
        Note note = new Note("Title", collection, "Content");
        when(notesService.getNoteById(noteId, collection.collectionKey)).thenReturn(Optional.of(note));
        ResponseEntity<Note> response = controller.getNoteById(noteId, collection.collectionKey);

        assertEquals(note, response.getBody());
        assertEquals(200, response.getStatusCode().value());
        verify(notesService).getNoteById(noteId, collection.collectionKey);
    }

    @Test
    void updateNoteTest_NotFound() {
        Collection collection = new Collection("collection key");
        long noteId = random.nextLong();
        when(notesService.isTitleUniqueInCollection(any(), any(), anyLong())).thenReturn(true);

        when(notesService.updateNoteTitleAndContent(eq(noteId), any(Note.class), any(String.class))).thenReturn(Optional.empty());
        ResponseEntity<Object> expectedResponse = ResponseEntity.notFound().build();
        when(errorResponseBuilder.buildNotFound(any()))
                .thenReturn(expectedResponse);

        Note validNote = new Note("Valid Title", collection, "Valid Content");
        validNote.id = noteId;

        ResponseEntity<Note> response = controller.updateNote(noteId, validNote.collection.collectionKey, null, validNote);

        assertEquals(expectedResponse, response);
        verify(notesService).updateNoteTitleAndContent(eq(noteId), any(Note.class), eq(validNote.collection.collectionKey));
        verify(errorResponseBuilder).buildNotFound(any());
    }
    @Test
    void updateNoteTest_ServerError() {
        Collection collection = new Collection("collection key");
        long noteId = random.nextLong();
        when(notesService.isTitleUniqueInCollection(any(), any(), anyLong())).thenReturn(true);
        Note note = new Note("Updated Title", collection, "Updated Content");
        note.id = noteId;
        RuntimeException exception = new RuntimeException("Server error");
        ResponseEntity<Object> expectedResponse = ResponseEntity.internalServerError().build();

        when(notesService.updateNoteTitleAndContent(noteId, note, note.collection.collectionKey)).thenThrow(exception);
        when(errorResponseBuilder.buildServerError(exception)).thenReturn(expectedResponse);

        ResponseEntity<Note> response = controller.updateNote(noteId, note.collection.collectionKey, null, note);

        assertEquals(expectedResponse, response);
        verify(notesService).updateNoteTitleAndContent(noteId, note, note.collection.collectionKey);
        verify(errorResponseBuilder).buildServerError(exception);
    }

    @Test
    void updateNoteTest_Conflict() {
        Collection collection = new Collection("Test Collection");
        long noteId = random.nextLong();
        Note note = new Note("Duplicate Title", collection, "Content");
        note.id = noteId;

        when(notesService.isTitleUniqueInCollection(any(), any(), anyLong())).thenReturn(false);

        ResponseEntity<Note> response = controller.updateNote(noteId, note.collection.collectionKey, null, note);

        verify(notesService, never()).updateNoteTitleAndContent(anyLong(), any(Note.class), any());
        verify(errorResponseBuilder).buildConflict(any());
        assertEquals(errorResponseBuilder.buildConflict(any()), response);
    }



    @Test
    void deleteNoteByIdTest_NotFound() {
        long noteId = random.nextLong();
        String noteCollectionKey = "collection-key";
        ResponseEntity<Object> expectedResponse = ResponseEntity.notFound().build();
        when(notesService.deleteNoteById(noteId, noteCollectionKey)).thenReturn(Optional.empty());
        when(errorResponseBuilder.buildNotFound(any()))
                .thenReturn(expectedResponse);

        ResponseEntity<Note> response = controller.deleteNoteById(noteId, noteCollectionKey);

        assertEquals(expectedResponse, response);
        verify(notesService).deleteNoteById(noteId, noteCollectionKey);
        verify(errorResponseBuilder).buildNotFound(any());
    }

    @Test
    void getAllNotesAndCompareWithAllInfos() {
        Collection collection1 = new Collection("collection key 1");
        List<NoteIdentification> noteInfos = List.of(
                new NoteIdentification("Title 1", collection1),
                new NoteIdentification("Title 2", collection1),
                new NoteIdentification("Title 3", collection1)
        );
        when(notesService.getNotes(null, collection1.collectionKey)).thenReturn(noteInfos);

        ResponseEntity<List<NoteIdentification>> response = controller.getNoteInfosByQuery(null, collection1.collectionKey);

        assertEquals(noteInfos, response.getBody());
        assertEquals(200, response.getStatusCode().value());
        verify(notesService).getNotes(null, collection1.collectionKey);
    }

    @Test
    void getNotesWithFilterToCompareWithInfos_Success() {
        String query = "test";
        Collection collection = new Collection("collection key");
        List<NoteIdentification> filteredNotes = List.of(new NoteIdentification("Filtered Title", collection));
        when(notesService.getNotes(query, collection.collectionKey)).thenReturn(filteredNotes);

        ResponseEntity<List<NoteIdentification>> response = controller.getNoteInfosByQuery(query, collection.collectionKey);

        assertEquals(filteredNotes, response.getBody());
        assertEquals(200, response.getStatusCode().value());
        verify(notesService).getNotes(query, collection.collectionKey);
    }
}