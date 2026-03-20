package serviceTests;

import client.exceptions.ApiResponseValidator;
import client.exceptions.ServerValidator;
import client.services.ConfigService;
import client.services.LanguageService;
import client.services.NotesMigrationService;
import client.services.NotesService;
import client.threading.PushThread;
import client.utils.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import commons.Collection;
import commons.CollectionConfig;
import commons.Note;
import commons.NoteIdentification;
import commons.constants.AppConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class NotesServiceTest {

    private NotesService notesService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Collection collectionExpected = new Collection("collection-key");
    private final Note noteExpected = new Note("title", collectionExpected, "content");
    private final NoteIdentification noteIdentificationExpected = new NoteIdentification(noteExpected);
    private final long noteIdExpected = 42L;

    @RegisterExtension
    static WireMockExtension wireMockRule = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().port(8080))
            .build();

    @AfterEach
    void tearDown() {
        WireMock.reset();
    }

    @BeforeEach
    public void setUp(){
        NotesClient notesApiClient = new NotesApiClient(
                new ApiResponseValidator(null),
                new ServerValidator(new TestHealthCheckClient(), new LanguageService()));
        FilesClient filesClient = new FilesApiClient(
                new ApiResponseValidator(null),
                new ServerValidator(new TestHealthCheckClient(), new LanguageService()),
                new LanguageService());
        NotesMigrationService migrationService = new NotesMigrationService(notesApiClient, filesClient);

        PushThread pushThread = new PushThread(migrationService);
        ConfigService configService = new ConfigService("Test", null);
        notesService = new NotesService(notesApiClient, pushThread, configService, migrationService);
        notesService.initializeNotes(); // Ensure the service is initialized
        noteIdentificationExpected.setId(noteIdExpected);
        noteExpected.setId(noteIdExpected);
        notesService.setSidebarData(new ArrayList<>(List.of(noteIdentificationExpected)));
        configService.addCollectionConfig(
                new CollectionConfig(
                        "collection-key",
                        "Title",
                        AppConstants.DefaultServerUrl));
    }

    @Test
    public void testInitializeNotes() {
        notesService.initializeNotes();
        assertNotNull(notesService.getNoteInfos());
        assertTrue(notesService.getNoteInfos().isEmpty());
    }

    @Test
    public void testPullNoteAtIndex_validIndex() throws JsonProcessingException {
        WireMock.stubFor(
                WireMock.get("/api/notes/" + noteExpected.collection.collectionKey + "/" + noteIdExpected)
                        .willReturn(
                                WireMock.aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(objectMapper.writeValueAsString(noteExpected))
                                        .withStatus(200)
                        )
        );
        notesService.pullNoteAtIndex(0);
        Optional<Note> note = notesService.pullNoteAtIndex(0);
        assertTrue(note.isPresent());
        assertEquals(noteExpected.title, note.get().title);
    }

    @Test
    public void testPullNoteAtIndex_invalidIndex() {
        Optional<Note> note = notesService.pullNoteAtIndex(10); // Out of bounds
        assertFalse(note.isPresent());
    }

    @Test
    public void testGetNoteInfoAtIndex_validIndex() {
        Optional<NoteIdentification> noteInfo = notesService.getNoteInfoAtIndex(0);
        assertTrue(noteInfo.isPresent());
        assertEquals("title", noteInfo.get().title);
    }

    @Test
    public void testGetNoteInfoAtIndex_invalidIndex() {
        Optional<NoteIdentification> noteInfo = notesService.getNoteInfoAtIndex(10);
        assertFalse(noteInfo.isPresent());
    }

    @Test
    public void testGetIndexOfNoteId() {
        var result = notesService.getIndexOfNoteId(42L);
        assertTrue(result.isPresent());
        assertEquals(0,
                result.getAsInt());
    }

    @Test
    public void testIsValidIndex() {
        assertTrue(notesService.isValidIndex(0)); // valid note
        assertFalse(notesService.isValidIndex(1)); // Out of bounds
    }

    @Test
    public void testDeleteNote() throws JsonProcessingException {
        WireMock.stubFor(
                WireMock.delete("/api/notes/" + noteExpected.collection.collectionKey + "/" + noteIdExpected)
                        .willReturn(
                                WireMock.aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(objectMapper.writeValueAsString(noteExpected))
                                        .withStatus(200)
                        )
        );
        NoteIdentification noteToDelete = notesService.getNoteInfos().getFirst();
        notesService.deleteNote(noteToDelete);
        assertEquals(0, notesService.getNoteInfos().size()); // One note should be removed
    }

    @Test
    public void testPushNote() throws JsonProcessingException {
        WireMock.stubFor(
                WireMock.get("/api/notes/" + noteExpected.collection.collectionKey + "/" + noteIdExpected)
                        .willReturn(
                                WireMock.aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(objectMapper.writeValueAsString(noteExpected))
                                        .withStatus(200)
                        )
        );
        Note note = notesService.pullNoteAtIndex(0).orElse(null);
        assertEquals(noteExpected, note);
    }

    @Test
    public void testUpdateNote() throws JsonProcessingException {
        WireMock.stubFor(
                WireMock.get("/api/notes/" + noteExpected.collection.collectionKey + "/" + noteIdExpected)
                        .willReturn(
                                WireMock.aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(objectMapper.writeValueAsString(noteExpected))
                                        .withStatus(200)
                        )
        );
        notesService.pullNoteAtIndex(0);
        notesService.updateNote(0, "Updated Title", "Updated Content");
        NoteIdentification updatedNoteInfo = notesService.getNoteInfos().getFirst();
        assertEquals("Updated Title", updatedNoteInfo.title);
    }

    @Test
    public void testPushAndUpdateNote() throws JsonProcessingException {
        WireMock.stubFor(
                WireMock.get("/api/notes/" + noteExpected.collection.collectionKey + "/" + noteIdExpected)
                        .willReturn(
                                WireMock.aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(objectMapper.writeValueAsString(noteExpected))
                                        .withStatus(200)
                        )
        );
        notesService.pullNoteAtIndex(0);
        notesService.updateNote(0, "Updated Title", "Updated Content");
        Note noteUpdated = new Note("Updated Title", collectionExpected, "Updated Content");
        noteUpdated.setId(noteIdExpected);
        NoteIdentification noteIdentification = notesService.getNoteInfos().getFirst();
        assertEquals("Updated Title", noteIdentification.title);
    }
}
