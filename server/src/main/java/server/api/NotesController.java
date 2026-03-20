package server.api;

import commons.Collection;
import commons.Note;
import commons.NoteIdentification;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.FilesService;
import service.NotesService;
import server.ErrorResponseBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NotesController {

    private final NotesService notesService;
    private final ErrorResponseBuilder errorResponseBuilder;
    private final FilesService filesService;

    // Constructor using NotesService and errorResponseBuilder
    public NotesController(
            NotesService notesService,
            ErrorResponseBuilder errorResponseBuilder,
            FilesService fileService) {
        this.notesService = notesService;
        this.errorResponseBuilder = errorResponseBuilder;
        this.filesService = fileService;
    }

    /**
     * REST API endpoint for creating notes
     * @param collectionKey The collection of the new note
     * @return Response entity that (if successful) contains id of the created note
     */
    @PostMapping("/{collectionKey}/{title}")
    public ResponseEntity<Note> createNote(
            @PathVariable("collectionKey") String collectionKey,
            @PathVariable("title") String title) {
        try {
            var collection = notesService.getCollection(collectionKey);

            if (title.isEmpty())
                return errorResponseBuilder
                        .buildBadRequest("Item.titleCannotBeEmpty");

            else if (collection.isEmpty())
                errorResponseBuilder.buildNotFound(
                        "Item.collectionOfCreatedNoteDoesNotExist");

            else if (!notesService.isTitleUniqueInCollection(collection.get(), title, -1L))
                return errorResponseBuilder.buildConflict(
                        "Item.titleAlreadyExists");

            return notesService.createNote(collectionKey, title)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> errorResponseBuilder.buildNotFound(
                            "Item.unexpectedErrorOccurredWhenCreatingNote"));
        } catch (Exception e) {
            return errorResponseBuilder.buildServerError(e);
        }
    }

    /**
     * REST API endpoint for getting a note by noteId
     * If the note with the given noteId does not
     * exist in the database, bad request is returned
     *
     * @param noteId The id of the note
     * @return The note with the given id from the database
     */
    @GetMapping("/{collectionKey}/{noteId}")
    public ResponseEntity<Note> getNoteById(
            @PathVariable("noteId") long noteId,
            @PathVariable("collectionKey") String collectionKey) {
        try {
            return notesService.getNoteById(noteId, collectionKey)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> errorResponseBuilder.buildNotFound(
                            "Item.noteNotFoundInCollection"));
        } catch (Exception e) {
            return errorResponseBuilder.buildServerError(e);
        }
    }


    /**
     * REST API endpoint for updating the title of the note
     * If the note with the given noteId is found,
     * it's title and content is then updated in the database
     *
     * @param noteId  the noteId of the note
     * @return the updated note with the new title
     */

    @PutMapping("/{collectionKey}/{noteId}")
    public ResponseEntity<Note> updateNote(
            @PathVariable("noteId") long noteId,
            @PathVariable("collectionKey") String collectionKey,
            @Param("newCollectionKey") String newCollectionKey,
            @RequestBody Note note) {
        try {
            if (note == null || note.title == null ||
                    note.content == null || note.collection == null || noteId != note.id) {
                return errorResponseBuilder
                        .buildBadRequest("Item.noteIsIncomplete");
            }

            if (note.title.isEmpty()) {
                return errorResponseBuilder
                        .buildBadRequest("Item.titleCannotBeEmpty");
            }

            if (!notesService.isTitleUniqueInCollection(note.collection, note.title, noteId)) {
                return errorResponseBuilder.buildConflict(
                        "Item.titleAlreadyExists");
            }

            var newNote = notesService.updateNoteTitleAndContent(noteId, note, collectionKey);

            if (newNote.isEmpty()) {
                return errorResponseBuilder.buildNotFound(
                                "Item.noteNotFoundInCollection");

            }
            if (newCollectionKey == null) {
                return ResponseEntity.ok(newNote.get());
            }

            return updateNoteCollection(note, newCollectionKey);

        } catch (Exception e) {
            return errorResponseBuilder.buildServerError(e);
        }
    }

    public ResponseEntity<Note> updateNoteCollection(Note note, String newCollectionKey) {
        if (!notesService.isTitleUniqueInCollection(
                new Collection(newCollectionKey),
                note.title,
                note.id)) {
            return errorResponseBuilder.buildConflict(
                    "Item.titleAlreadyExistsInCollection");
        }

        if (notesService.isCollectionInvalid(newCollectionKey)) {
            return errorResponseBuilder
                    .buildNotFound("Item.collectionAlreadyDeleted");
        }

        return notesService.updateNoteCollection(note.id, newCollectionKey)
                .map(ResponseEntity::ok)
                .orElseGet(() -> errorResponseBuilder.buildNotFound(
                        "Item.noteNotInOldCollection"));
    }

    /**
     * REST API endpoint for deleting notes
     * If the note with the given noteId is not found in the database
     * bad request is returned
     * If the noteId is in the database, the note with that noteId is deleted
     *
     * @param noteId the noteId of the note
     * @return the note that was deleted
     */
    @DeleteMapping("/{collectionKey}/{noteId}")
    public ResponseEntity<Note> deleteNoteById(
            @PathVariable("noteId") long noteId,
            @PathVariable("collectionKey") String collectionKey) {
        try {
            return notesService.deleteNoteById(noteId, collectionKey)
                    .map(deletedNote -> filesService.deleteNoteFiles(noteId)
                            .map(_ -> ResponseEntity.ok(deletedNote))
                            .orElseGet(() -> errorResponseBuilder.buildBadRequest(
                                    "Item.noteDeletedButFilesNot")))
                    .orElseGet(() -> errorResponseBuilder.buildNotFound(
                            "Item.noteNotInThisCollection"));
        } catch (Exception e) {
            return errorResponseBuilder.buildServerError(e);
        }
    }

    /**
     * REST API endpoint for querying all notes
     *
     * @param query optional string to filter the notes
     * containing the string in the title or content
     * @return returns list of all notes' info, title if query is null
     * else filtered notes' info otherwise
     */
    @GetMapping("/{collectionKey}")
    public ResponseEntity<List<NoteIdentification>> getNoteInfosByQuery(
            @RequestParam(value = "query", required = false) String query,
            @PathVariable("collectionKey") String collectionKey) {
        try {
            List<NoteIdentification> notes = notesService.getNotes(query, collectionKey);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            return errorResponseBuilder.buildServerError(e);
        }
    }
}