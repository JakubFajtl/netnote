package server.api;

import commons.EmbeddedFile;
import commons.EmbeddedFileIdentification;
import commons.FileKey;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import server.ErrorResponseBuilder;
import service.FilesService;

import java.io.FileNotFoundException;


import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/files")
public class FilesController {

    private final FilesService filesService;
    private final ErrorResponseBuilder errorResponseBuilder;

    public FilesController(FilesService filesService, ErrorResponseBuilder errorResponseBuilder) {
        this.filesService = filesService;
        this.errorResponseBuilder = errorResponseBuilder;
    }

    @PostMapping("/{noteId}")
    public ResponseEntity<EmbeddedFile> addFileToNote(
            @PathVariable("noteId") long noteId,
            @RequestPart("file") MultipartFile file) {
        try {
            EmbeddedFile newFile = filesService.createFile(file.getOriginalFilename(),
                    file.getContentType(),
                    file.getInputStream(), noteId);
            return ResponseEntity.ok(newFile);
        } catch (Exception e) {
            return errorResponseBuilder.buildServerError(e);
        }
    }

    @GetMapping("/{noteId}/{fileName}")
    public ResponseEntity<byte[]> getFileByName(@PathVariable("noteId") long noteId,
                                                @PathVariable("fileName") String fileName){
        try {
            Optional<EmbeddedFile> optionalReturnValue = filesService
                    .getFileById(new FileKey(noteId, fileName));
            EmbeddedFile returnValue = optionalReturnValue.orElse(null);
            return filesService.buildResponseEntityByteArray(returnValue);
        } catch (Exception e) {
            return errorResponseBuilder.buildServerError(e);
        }

    }
    @GetMapping("/{noteId}")
    public ResponseEntity<byte[]> getAllFilesByNoteId(@PathVariable("noteId") long noteId) {
        try {
            Optional<List<EmbeddedFileIdentification>> optionalReturnValue = Optional
                    .of(filesService.getFileIdentifications(noteId));
            List<EmbeddedFileIdentification> returnValue = optionalReturnValue.orElse(null);
            return filesService.buildResponseEntityByteArray(returnValue);
        } catch (Exception e) {
            return errorResponseBuilder.buildServerError(e);
        }
    }

    /* if requested from a browser, it previews the file, also used for markdown
     * @param collectionName collection of the file, can be empty
     * @param noteId id of the note
     * @param fileName name of the file
     * @return the file
     */
    @GetMapping("/{collectionName}/{noteId}/{fileName}")
    public ResponseEntity<Resource> getFile(
            @PathVariable("collectionName") String collectionName,
            @PathVariable("noteId") long noteId,
            @PathVariable("fileName") String fileName) {
        try {
            // Fetch the file
            Optional<EmbeddedFile> optionalReturnValue = filesService
                    .getFileById(new FileKey(noteId, fileName));
            if (optionalReturnValue.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            EmbeddedFile returnValue = optionalReturnValue.get();

            // Build the resource
            ByteArrayResource resource = new ByteArrayResource(returnValue.file);

            // Determine the content type (e.g., image/png, image/jpeg)
            String contentType = returnValue.contentType;
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream"; // Fallback content type
            }

            // Build the response entity
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName +"\"")
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(resource);

        } catch (Exception e) {
            return errorResponseBuilder.buildServerError(e);
        }
    }

    @DeleteMapping("/{noteId}/{fileName}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable long noteId,
            @PathVariable String fileName) {
        try {
            filesService.deleteFile(noteId, fileName);
            return ResponseEntity.noContent().build();
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{noteId}/{fileName}")
    public ResponseEntity<Void> renameFile(
            @PathVariable("noteId") long noteId,
            @PathVariable("fileName") String fileName,
            @RequestParam("newName") String newName) {
        try {
            filesService.renameFile(noteId, fileName, newName);
            return ResponseEntity.noContent().build();

        } catch (FileNotFoundException e) {
            return ResponseEntity.status(404).build(); // File not found

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null); // Name already exists

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



}
