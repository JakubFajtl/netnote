package service;

import commons.EmbeddedFile;
import commons.EmbeddedFileIdentification;
import commons.FileKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server.ErrorResponseBuilder;
import server.database.FilesRepository;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FilesServiceTest {

    @Mock
    private FilesRepository filesRepository;

    @Mock
    private ErrorResponseBuilder errorResponseBuilder;

    private FilesService filesService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filesService = new FilesService(filesRepository, errorResponseBuilder);
    }

    @Test
    void testCreateFile_Success() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        EmbeddedFile file = new EmbeddedFile("file.txt", "text/plain", inputStream, 1L);

        when(filesRepository.save(any(EmbeddedFile.class))).thenReturn(file);

        EmbeddedFile createdFile = filesService.createFile("file.txt", "text/plain", inputStream, 1L);

        assertNotNull(createdFile);
        assertEquals("file.txt", createdFile.getFileKey().getFilename());
        verify(filesRepository).save(any(EmbeddedFile.class));
    }

    @Test
    void testGetFileById_Success() {
        FileKey fileKey = new FileKey(1L, "file.txt");

        EmbeddedFile file = new EmbeddedFile();
        file.setFileKey(1L, "file.txt");

        when(filesRepository.findById(fileKey)).thenReturn(Optional.of(file));

        Optional<EmbeddedFile> result = filesService.getFileById(fileKey);

        assertTrue(result.isPresent());
        assertEquals(fileKey.getNoteId(), result.get().getFileKey().getNoteId());
        assertEquals(fileKey.getFilename(), result.get().getFileKey().getFilename());
        verify(filesRepository).findById(fileKey);
    }



    @Test
    void testGetFileById_NotFound() {
        FileKey fileKey = new FileKey(1L, "file.txt");

        when(filesRepository.findById(fileKey)).thenReturn(Optional.empty());

        Optional<EmbeddedFile> result = filesService.getFileById(fileKey);

        assertFalse(result.isPresent());
        verify(filesRepository).findById(fileKey);
    }

    @Test
    void testGetFiles_Success() {
        long noteId = 1L;
        List<EmbeddedFile> files = List.of(new EmbeddedFile(), new EmbeddedFile());

        when(filesRepository.findByFileKeyNotid(noteId)).thenReturn(files);

        List<EmbeddedFile> result = filesService.getFiles(noteId);

        assertEquals(2, result.size());
        verify(filesRepository).findByFileKeyNotid(noteId);
    }

    @Test
    void testGetFileIdentifications_Success() {
        long noteId = 1L;
        EmbeddedFile file = new EmbeddedFile();
        file.setFileKey(noteId, "file.txt");

        when(filesRepository.findByFileKeyNotid(noteId)).thenReturn(List.of(file));

        List<EmbeddedFileIdentification> result = filesService.getFileIdentifications(noteId);

        assertEquals(1, result.size());
        assertEquals("file.txt", result.getFirst().getFileKey().getFilename());
        verify(filesRepository).findByFileKeyNotid(noteId);
    }

    @Test
    void testDeleteNoteFiles_Success() {
        long noteId = 1L;
        List<EmbeddedFile> files = List.of(new EmbeddedFile(), new EmbeddedFile());

        when(filesRepository.findByFileKeyNotid(noteId)).thenReturn(files);

        Optional<List<EmbeddedFile>> result = filesService.deleteNoteFiles(noteId);

        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        verify(filesRepository).deleteAll(files);
    }

    @Test
    void testDeleteFile_Success() throws FileNotFoundException {
        long noteId = 1L;
        EmbeddedFile file = new EmbeddedFile();
        file.setFileKey(noteId, "file.txt");

        when(filesRepository.findByFileKeyNotid(noteId)).thenReturn(List.of(file));

        filesService.deleteFile(noteId, "file.txt");

        verify(filesRepository).delete(file);
    }

    @Test
    void testDeleteFile_NotFound() {
        long noteId = 1L;

        when(filesRepository.findByFileKeyNotid(noteId)).thenReturn(List.of());

        Exception exception = assertThrows(FileNotFoundException.class, () ->
                filesService.deleteFile(noteId, "file.txt"));

        assertEquals("Item.fileNotFound", exception.getMessage());
        verify(filesRepository, never()).delete(any());
    }

    @Test
    void testRenameFile_Success() throws FileNotFoundException {
        long noteId = 1L;
        EmbeddedFile file = new EmbeddedFile();
        file.setFileKey(noteId, "oldName.txt");

        when(filesRepository.findByFileKeyNotid(noteId)).thenReturn(List.of(file));
        when(filesRepository.save(any(EmbeddedFile.class))).thenReturn(file);

        filesService.renameFile(noteId, "oldName.txt", "newName.txt");

        verify(filesRepository).delete(file);
        verify(filesRepository).save(any(EmbeddedFile.class));
    }

    @Test
    void testRenameFile_NotFound() {
        long noteId = 1L;

        when(filesRepository.findByFileKeyNotid(noteId)).thenReturn(List.of());

        Exception exception = assertThrows(FileNotFoundException.class, () ->
                filesService.renameFile(noteId, "oldName.txt", "newName.txt"));

        assertEquals("Item.fileNotFound", exception.getMessage());
        verify(filesRepository, never()).delete(any());
    }

    @Test
    void testRenameFile_NameConflict() {
        long noteId = 1L;
        EmbeddedFile file1 = new EmbeddedFile();
        file1.setFileKey(noteId, "oldName.txt");
        EmbeddedFile file2 = new EmbeddedFile();
        file2.setFileKey(noteId, "newName.txt");

        when(filesRepository.findByFileKeyNotid(noteId)).thenReturn(List.of(file1, file2));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                filesService.renameFile(noteId, "oldName.txt", "newName.txt"));

        assertEquals("Item.fileNameAlreadyExists", exception.getMessage());
        verify(filesRepository, never()).delete(any());
    }
}
