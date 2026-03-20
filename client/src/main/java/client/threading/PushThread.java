package client.threading;

import client.services.NotesMigrationService;
import com.google.inject.Inject;
import commons.CollectionConfig;
import commons.Note;

import java.util.Optional;

public class PushThread extends Thread {
    private NotesMigrationService notesMigrationService;
    boolean running = true;
    private Optional<Note> changedNote;
    private CollectionConfig currentCollectionConfig;
    private CollectionConfig newCollectionConfig;

    @Inject
    public PushThread(NotesMigrationService notesMigrationService) {
        this.notesMigrationService = notesMigrationService;
        this.changedNote = Optional.empty();

        newCollectionConfig = null;
    }

    public void setNoteToChange(
            Note note,
            CollectionConfig currentCollectionConfig,
            CollectionConfig newCollectionConfig) {
        synchronized (this) {
            this.changedNote = Optional.of(note);
            this.newCollectionConfig = newCollectionConfig;
            this.currentCollectionConfig = currentCollectionConfig;
        }
    }

    /**
     * Pushes changed note if any unpushed changes
     */
    public void pushToServer() {
        if (changedNote.isPresent()) {
            try {
                notesMigrationService.migrateNote(
                        changedNote.get(),
                        currentCollectionConfig,
                        newCollectionConfig);

            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                changedNote = Optional.empty();
            }
        }
    }


    /**
     * Cancels the next push until a change in notes is made
     */
    public void pushNow() {
        synchronized (this) {
            this.notify();
        }
    }

    /**
     * Sends currentNote to server every 5 sec (if modified)
     */
    @Override
    public void run() {
        while (running) {
            pushToServer();

            try {
                synchronized (this) {
                    this.wait(5000);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}