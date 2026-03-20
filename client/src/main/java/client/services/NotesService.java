package client.services;

import client.threading.PushThread;
import client.utils.NotesClient;
import commons.CollectionConfig;
import commons.Note;
import commons.NoteIdentification;
import jakarta.inject.Inject;

import java.util.*;
import java.util.stream.IntStream;

/**
 * This class is for operating the list of the notes
 */
public class NotesService {
    private final NotesClient server;
    private final NotesMigrationService notesMigrationService;
    private List<NoteIdentification> sidebarData;
    private Note currentNote;
    private PushThread pushThread;
    private ConfigService configService;

    @Inject
    public NotesService(
            NotesClient server,
            PushThread pushThread,
            ConfigService configService,
            NotesMigrationService notesMigrationService) {
        this.server = server;
        this.pushThread = pushThread;
        this.configService = configService;
        this.notesMigrationService = notesMigrationService;
    }

    public void initializeNotes() {
        sidebarData = new ArrayList<>();
        if (!this.pushThread.isAlive()) {
            this.pushThread.start();
        }
    }

    /**
     * Restart thread if interrupted by exception
     */
    public void checkThread() {
        if (!this.pushThread.isAlive()) {
            this.pushThread = new PushThread(notesMigrationService);
            this.pushThread.start();
        }
    }

    public void setSidebarData(List<NoteIdentification> notes) {
        sidebarData = notes;
    }

    public Note getCurrentNote() { return currentNote; }

    /**
     * Pulls the full note from the server by the index within the list
     * Pushes the previously selected note (if any changes since the last push)
     *
     * @param index of the note within the current NoteInfo list
     * @return the full note at that index
     */
    public Optional<Note> pullNoteAtIndex(int index){
        var noteInfo = getNoteInfoAtIndex(index);
        if (noteInfo.isEmpty()) return Optional.empty();

        pushThread.pushNow(); //Push the previously selected note

        currentNote = server.pullNote(
                noteInfo.get().id,
                getConfig(noteInfo.get().collection.collectionKey));
        return Optional.of(currentNote);
    }

    public Optional<NoteIdentification> getNoteInfoAtIndex(int index) {
        if (!isValidIndex(index)) return Optional.empty();

        return Optional.of(sidebarData.get(index));
    }

    /**
     * Returns an index of a note with a specific id (if found)
     * @param noteId the id of the note to find
     * @return the index of a note or Optional.empty() if no note was found
     */
    public OptionalInt getIndexOfNoteId(long noteId) {
        return IntStream
                .range(0, sidebarData.size())
                .filter(i -> sidebarData.get(i).id == noteId)
                .findFirst();
    }

    public OptionalInt getIndexOfNoteTitle(String title) {
        return IntStream
                .range(0, sidebarData.size())
                .filter(i -> sidebarData.get(i).title.equals(title))
                .findFirst();
    }

    public CollectionConfig getConfig(String collectionKey) {
        return configService.getCollectionConfig(collectionKey);
    }

    public boolean isValidIndex(int index) {
        return index >= 0 && index < sidebarData.size();
    }


    public void createNote(String title, CollectionConfig config) {
        currentNote = server.createNote(title, config);
        sidebarData.add(new NoteIdentification(currentNote));
    }

    public void deleteNote(NoteIdentification noteInfo) {
        sidebarData.remove(noteInfo);
        server.deleteNote(noteInfo.id, getConfig(noteInfo.collection.collectionKey));
    }

    public List<NoteIdentification> getListOfNotes() {
        return sidebarData;
    }

    /**
     * Updates the collection of the note
     * @param index - index of the note
     * @param newCollectionConfig - config of the new collection
     */
    public void updateNoteCollection(int index, CollectionConfig newCollectionConfig) {
        var note = pullNoteAtIndex(index);
        if (note.isEmpty())
            return;

        pushThread.setNoteToChange(
                note.get(),
                configService.getCollectionConfig(note.get().collection.collectionKey),
                newCollectionConfig);
        pushThread.pushToServer();
    }

    // Updates the content and the title of the note
    public void updateNote(int index, String newTitle, String newContent){
        currentNote.title = newTitle;
        currentNote.content = newContent;

        pushThread.setNoteToChange(
                currentNote,
                configService.getCollectionConfig(currentNote.collection.collectionKey),
                null);

        sidebarData.set(index, new NoteIdentification(currentNote));
    }

    public void updateNoteTitle(int index, String newTitle) {
        updateNote(index, newTitle, currentNote.content);
    }

    public void updateNoteContent(int index, String newContent) {
        updateNote(index, currentNote.title, newContent);
    }

    /**
     * This method checks if the note is the first one in its collection
     * We need this method, since all notes are in one list
     * The implementation checks if there is a note above this note
     * that belongs to a different collection
     * @param index of the note
     * @return the boolean indicating if it is the fist cell
     */
    public boolean isFirstCellInCollection(int index) {
        var info = getNoteInfoAtIndex(index);
        if (info.isPresent()) {
            var infoAbove = getNoteInfoAtIndex(index - 1);
            return infoAbove.map(noteIdentification -> !Objects.equals(
                    noteIdentification.collection.collectionKey,
                    info.get().collection.collectionKey)).orElse(true);
        }

        return false;
    }

    public boolean isLastCellInCollection(int index) {
        return index == sidebarData.size() - 1;
    }

    //getter for testing
    public List<NoteIdentification> getNoteInfos() { return sidebarData; }

    public void pushSelected() {
        pushThread.pushToServer(); }

    public boolean isTitleUniqueInCollection(String collectionKey, String newTitle,
                                             int currentNoteIndex) {
        List<NoteIdentification> notesInCollection = sidebarData.stream()
                .filter(note -> note.collection.collectionKey.equals(collectionKey))
                .toList();

        for (NoteIdentification note : notesInCollection) {

            if (note.id != sidebarData.get(currentNoteIndex).id
                    && note.title.equalsIgnoreCase(newTitle)) {
                return false;
            }
        }
        return true;
    }

}
