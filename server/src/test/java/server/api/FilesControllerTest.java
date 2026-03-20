package server.api;

import commons.EmbeddedFile;
import commons.EmbeddedFileIdentification;
import commons.FileKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import server.ErrorResponseBuilder;
import service.FilesService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilesControllerTest {

    @Mock
    private FilesService filesService;

    @Mock
    private ErrorResponseBuilder errorResponseBuilder;

    private FilesController controller;

    @BeforeEach
    void setUp() {
        controller = new FilesController(filesService, errorResponseBuilder);
    }

    @Test
    void addFileToNote_Success() throws Exception {
        long noteId = 123L;
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.png");
        when(file.getContentType()).thenReturn("image/png");
        InputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        when(file.getInputStream()).thenReturn(inputStream);

        EmbeddedFile expectedEmbeddedFile = new EmbeddedFile("test.png", "image/png", inputStream, noteId);
        when(filesService.createFile(eq("test.png"), eq("image/png"), eq(inputStream), eq(noteId)))
                .thenReturn(expectedEmbeddedFile);

        ResponseEntity<EmbeddedFile> response = controller.addFileToNote(noteId, file);

        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        assertEquals(expectedEmbeddedFile, response.getBody());
        verify(filesService, times(1))
                .createFile("test.png", "image/png", inputStream, noteId);
    }

    @Test
    void addFileToNote_ServerError() throws Exception {
        long noteId = 123L;
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.png");
        when(file.getContentType()).thenReturn("image/png");
        InputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        when(file.getInputStream()).thenReturn(inputStream);

        Exception exception = new RuntimeException("Test exception");

        ResponseEntity<Object> expectedErrorResponse = ResponseEntity.internalServerError().build();

        when(filesService.createFile(eq("test.png"), eq("image/png"), eq(inputStream), eq(noteId)))
                .thenThrow(exception);
        when(errorResponseBuilder.buildServerError(any(Exception.class))).thenReturn(expectedErrorResponse);

        ResponseEntity<EmbeddedFile> response = controller.addFileToNote(noteId, file);

        assertEquals(expectedErrorResponse, response);
        verify(filesService, times(1)).createFile(eq("test.png"), eq("image/png"), eq(inputStream), eq(noteId));
        verify(errorResponseBuilder, times(1)).buildServerError(any(Exception.class));
    }



    @Test
    void getFileByName_Success() throws Exception {
        long noteId = 123L;
        String fileName = "test.png";

        EmbeddedFile fileInDb = new EmbeddedFile(fileName, "image/png", new ByteArrayInputStream(new byte[]{1, 2, 3}), noteId);
        when(filesService.getFileById(new FileKey(noteId, fileName)))
                .thenReturn(Optional.of(fileInDb));

        ResponseEntity<byte[]> expectedResponse = ResponseEntity.ok(new byte[]{1, 2, 3});
        when(filesService.buildResponseEntityByteArray(fileInDb)).thenReturn(expectedResponse);

        ResponseEntity<byte[]> response = controller.getFileByName(noteId, fileName);

        assertEquals(expectedResponse, response);
        verify(filesService, times(1)).getFileById(new FileKey(noteId, fileName));
        verify(filesService, times(1)).buildResponseEntityByteArray(fileInDb);
    }

    @Test
    void getFileByName_ServerError() {
        long noteId = 123L;
        String fileName = "test.png";

        RuntimeException exception = new RuntimeException("Test exception");

        ResponseEntity<byte[]> expectedErrorResponse = ResponseEntity.internalServerError().build();

        when(filesService.getFileById(any(FileKey.class))).thenThrow(exception);
        when(errorResponseBuilder.<byte[]>buildServerError(any(Exception.class))).thenReturn(expectedErrorResponse);

        ResponseEntity<byte[]> response = controller.getFileByName(noteId, fileName);

        assertEquals(expectedErrorResponse, response);
        verify(filesService, times(1)).getFileById(any(FileKey.class));
        verify(errorResponseBuilder, times(1)).buildServerError(any(Exception.class));
    }



    @Test
    void getAllFilesByNoteId_Success() throws Exception {
        long noteId = 123L;
        EmbeddedFileIdentification file1 = new EmbeddedFileIdentification("file1.png", "image/png", noteId);
        EmbeddedFileIdentification file2 = new EmbeddedFileIdentification("file2.png", "image/png", noteId);

        List<EmbeddedFileIdentification> fileList = List.of(file1, file2);
        when(filesService.getFileIdentifications(noteId)).thenReturn(fileList);

        ResponseEntity<byte[]> expectedResponse = ResponseEntity.ok(new byte[]{10, 20});
        when(filesService.buildResponseEntityByteArray(fileList)).thenReturn(expectedResponse);

        ResponseEntity<byte[]> response = controller.getAllFilesByNoteId(noteId);

        assertEquals(expectedResponse, response);
        verify(filesService, times(1)).getFileIdentifications(noteId);
        verify(filesService, times(1)).buildResponseEntityByteArray(fileList);
    }

    @Test
    void getAllFilesByNoteId_ServerError() {
        long noteId = 123L;

        RuntimeException exception = new RuntimeException("Test exception");

        ResponseEntity<byte[]> expectedErrorResponse = ResponseEntity.internalServerError().build();

        when(filesService.getFileIdentifications(noteId)).thenThrow(exception);
        when(errorResponseBuilder.<byte[]>buildServerError(any(Exception.class))).thenReturn(expectedErrorResponse);

        ResponseEntity<byte[]> response = controller.getAllFilesByNoteId(noteId);

        assertEquals(expectedErrorResponse, response);
        verify(filesService, times(1)).getFileIdentifications(noteId);
        verify(errorResponseBuilder, times(1)).buildServerError(any(Exception.class));
    }



    @Test
    void deleteFile_Success() throws Exception {
        long noteId = 123L;
        String fileName = "test.png";

        ResponseEntity<Void> response = controller.deleteFile(noteId, fileName);

        assertEquals(HttpStatusCode.valueOf(204), response.getStatusCode());
        verify(filesService, times(1)).deleteFile(noteId, fileName);
    }

    @Test
    void deleteFile_ServerError() throws Exception {
        long noteId = 123L;
        String fileName = "test.png";
        doThrow(new RuntimeException()).when(filesService).deleteFile(noteId, fileName);

        ResponseEntity<Void> response = controller.deleteFile(noteId, fileName);

        assertEquals(HttpStatusCode.valueOf(500), response.getStatusCode());
        verify(filesService, times(1)).deleteFile(noteId, fileName);
    }
}
