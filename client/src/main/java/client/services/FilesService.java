package client.services;

import client.InformationalPopup;
import client.exceptions.NotesApiException;
import client.utils.FilesClient;
import commons.CollectionConfig;
import commons.EmbeddedFileIdentification;
import commons.NoteIdentification;
import commons.constants.AppConstants;
import jakarta.inject.Inject;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.springframework.core.io.Resource;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilesService {
    private final FilesClient server;
    private List<EmbeddedFileIdentification> currentFiles;
    private final LanguageService languageService;
    private final ConfigService configService;

    @Inject
    public FilesService(FilesClient server,
                        LanguageService languageService,
                        ConfigService configService) {
        this.server = server;
        currentFiles = new ArrayList<EmbeddedFileIdentification>();
        this.languageService = languageService;
        this.configService = configService;
    }

    public boolean isValidFileIndex(int index) {
        return index >= 0 && index < currentFiles.size();
    }

    public void uploadFileToNote(File file, NoteIdentification note){
        for(EmbeddedFileIdentification item : currentFiles){
            if(item.getFileKey().getFilename().equals(file.getName())){
                throw new NotesApiException(
                        languageService.getDescriptionByKey("Item.chooseDifferentFileName")
                        ,languageService.getDescriptionByKey("Item.fileNameAlreadyEmbedded") ,
                        null);
            }
        }
        server.uploadFileToNote(
                file,
                note.id,
                configService.getCollectionConfig(note.collection.collectionKey));
        getAllFilesFromNote(note);

    }

    public List<EmbeddedFileIdentification> getAllFilesFromNote(NoteIdentification note){
        currentFiles = server.getAllFilesFromNote(
                note.id,
                configService.getCollectionConfig(note.collection.collectionKey));
        return currentFiles;
    }

    public String addFile(File file, Optional<NoteIdentification> selectedNote){
        if(file != null && file.length() > AppConstants.maximumAllowedFileSizeInB){
            throw new NotesApiException(languageService.
                    getDescriptionByKey("Item.cantUploadLargerThan")  +
                    AppConstants.maximumAllowedFileSizeInB/(1024*1024)+
                    "MB",languageService.getDescriptionByKey("Item.fileTooBig") , null);
        }
        if(file != null && selectedNote.isPresent()){
            uploadFileToNote(file, selectedNote.get());
            EmbeddedFileIdentification currentFile = getAllFilesFromNote(selectedNote
                    .get()).getLast();
            if(currentFile.contentType.equals("image/jpg")
                    || currentFile.contentType.equals("image/jpeg")
                    || currentFile.contentType.equals("image/png")
                    || currentFile.contentType.equals("image/gif")) {
                return"\n!["+
                        currentFile.getFileKey().getFilename()+"](" +
                        currentFile.getFileKey().getFilename() + ")";
            }
        }
        return "";
    }

    public EmbeddedFileIdentification getFileFromIndex(int index) {
        return currentFiles.get(index);
    }

    public void downloadFile(int index, CollectionConfig fileCollection) throws IOException {
        if (!isValidFileIndex(index)) return;

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(languageService.getDescriptionByKey(
                "Item.chooseDestinationFolder"));
        File directory = directoryChooser.showDialog(new Stage());

        if (directory == null) return;

        EmbeddedFileIdentification embeddedFile = getFileFromIndex(index);
        String filename = embeddedFile.getFileKey().getFilename();

        Resource resource = server.getFileContentByName(
                embeddedFile.getFileKey().getNoteId(),
                filename,
                fileCollection);

        String filePath = Paths.get(directory.getAbsolutePath()).resolve(filename).toString();

        try (InputStream inputStream = resource.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        }

        new InformationalPopup().showInformation(
                languageService.getDescriptionByKey("Item.downloadSuccesful"),
                "'" + filename
                        + languageService.getDescriptionByKey("Item.successfullyDownloaded") +
                        directory.getName() + "'");
    }

    public void deleteFile(int index, CollectionConfig fileCollection) {
        if (!isValidFileIndex(index)) return;

        EmbeddedFileIdentification fileToDelete = getFileFromIndex(index);
        server.deleteFile(fileToDelete.getFileKey().getNoteId(),
                fileToDelete.getFileKey().getFilename(), fileCollection);

        // Refresh the file list after deletion
        long noteId = fileToDelete.getFileKey().getNoteId();
        currentFiles = server.getAllFilesFromNote(noteId, fileCollection);
    }
    public String createReferenceForFile(EmbeddedFileIdentification file, CollectionConfig config)
            throws UnsupportedEncodingException {
        return "![" + file.getFileKey()
                .getFilename() + "](" + file.getFilePath(config) + ")";
    }

    public void renameFile(
            long noteId,
            String oldName,
            String newName,
            CollectionConfig fileCollection) {
        server.renameFile(noteId, oldName, newName, fileCollection);
    }


    public String replaceImageLinks(String markdown,
                                    Optional<NoteIdentification> selectedNote,
                                    CollectionConfig selectedNoteCollection){
        if(markdown == null){
            return markdown;
        }
        // Regex to match Markdown image syntax
        String regex = "!\\[(.*?)\\]\\((.*?)\\)";
        String newMarkdown = markdown;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(markdown);

        // Iterate over all matches
        List<String> foundNames = new ArrayList();
        while (matcher.find()) {
            String fileName = matcher.group(2); // Captures the file path
            foundNames.add(fileName);
        }
        if(selectedNote.isEmpty()
                || selectedNote.equals(Optional.empty())
                || getAllFilesFromNote(selectedNote.get()) == null) {
            return markdown;
        }

        List<EmbeddedFileIdentification> currentFiles = getAllFilesFromNote(selectedNote.get());
        for (EmbeddedFileIdentification currentFile : currentFiles){
            try {
                for(String item : foundNames){
                    if(currentFile.getFileKey().getFilename().equals(item)){
                        newMarkdown = newMarkdown
                                .replaceAll("!\\[(.*?)\\]\\(" + Pattern.quote(item) + "\\)",
                                        Matcher.quoteReplacement(
                                                createReferenceForFile(currentFile,
                                                selectedNoteCollection)));
                    }
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return newMarkdown;
    }
    public String removeImageLinkFromMarkdown(String markdown, String fileName) {
        if (markdown == null || fileName == null) {
            return markdown;
        }

        String regex = "!\\[.*?\\]\\((.*?)" + Pattern.quote(fileName) + "(.*?)\\)";

        // Replace all markdown references with an empty string
        return markdown.replaceAll(regex, "");
    }
    public String updateImageLinksForRenamedFile(String markdown,
                                                 String oldFileName, String newFileName) {

        if (markdown == null || oldFileName == null || newFileName == null) {
            return markdown;
        }

        String regex = "!\\[(.*?)\\]\\((.*?)" + Pattern.quote(oldFileName) + "(.*?)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(markdown);

        StringBuilder updatedMarkdown = new StringBuilder();

        // Replace each occurrence of the old file name with the new file name
        while (matcher.find()) {
            String beforeFileName = matcher.group(2);
            String afterFileName = matcher.group(3);

            String updatedMarkdownSyntax = "![" +
                    Matcher.quoteReplacement(newFileName) + "]" +
                    "(" + beforeFileName +
                    Matcher.quoteReplacement(newFileName) + afterFileName + ")";

            matcher.appendReplacement(updatedMarkdown, updatedMarkdownSyntax);
        }
        matcher.appendTail(updatedMarkdown);

        return updatedMarkdown.toString();
    }

}
