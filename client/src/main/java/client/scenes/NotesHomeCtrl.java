package client.scenes;

import client.InformationalPopup;
import client.exceptions.EmptyTitleException;
import client.services.*;
import commons.*;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

public class  NotesHomeCtrl implements Initializable {

    private static final String BRITISH_FLAG = "/images/British-flag.png";
    private static final String DUTCH_FLAG = "/images/Dutch-flag.png";
    private static final String GERMANY_FLAG = "/images/German-flag.png";
    private static final String QUESTION_MARK = "/images/Questionmark-icon.jpg";
    private static final String FRISIAN_FLAG = "/images/Frisian-flag.jpg ";
    private static final String CAT_FLAG = "/images/Cat-flag-3.png";

    private static final Locale LOCALE_NL = Locale.forLanguageTag("nl-NL");
    private static final Locale LOCALE_FRI = new Locale.Builder().setLanguage("FRI").
            setRegion("NL").build();
    private static final Locale LOCALE_CAT = new Locale.Builder().setLanguage("CAT").
            setRegion("NL").build();

    private static final Map<Locale, String> localeToIcon = Map.of(
            Locale.ENGLISH, BRITISH_FLAG,
            Locale.GERMAN, GERMANY_FLAG,
            LOCALE_NL, DUTCH_FLAG,
            LOCALE_FRI, FRISIAN_FLAG,
            LOCALE_CAT, CAT_FLAG);

    NotesService service;
    EditCollectionsService editCollectionsService;
    MarkdownService md;
    MainCtrl mc;
    LanguageService languageService;
    ConfigService configService;
    FilesService fs;
    SelectCollectionsService selectCollectionsService;
    HelperCtrl helperCtrl;

    @Inject
    public NotesHomeCtrl(
            NotesService service,
            EditCollectionsService editCollectionsService,
            MarkdownService md,
            MainCtrl mc,
            ConfigService configService,
            LanguageService languageService,
            FilesService fs,
            SelectCollectionsService selectCollectionsService,
            HelperCtrl helperCtrl) {
        this.service = service;
        this.md = md;
        this.mc = mc;
        this.languageService = languageService;
        this.configService = configService;
        this.editCollectionsService = editCollectionsService;
        this.selectCollectionsService = selectCollectionsService;
        this.fs = fs;
        this.helperCtrl = helperCtrl;
    }

    @FXML
    private ListView<NoteIdentification> listViewNotesList;

    @FXML
    private ListView<String> listViewCollectionsList;

    @FXML
    private ListView<EmbeddedFileIdentification> listViewFilesList;

    @FXML
    private TextArea textAreaNoteContents;

    @FXML
    private WebView markdownWebView;

    @FXML
    private TextField textFieldNotesSearch;

    @FXML
    private Button searchButton;

    @FXML
    private Button collectionButton;

    @FXML
    private Button createBtn;

    @FXML
    private Button removeButton;

    @FXML
    private Button refreshButton;

    @FXML
    private MenuItem createSC;

    @FXML
    private MenuItem titleSC;

    @FXML
    private MenuItem deleteSC;

    @FXML
    private MenuItem searchbarSC;

    @FXML
    private MenuItem searchSC;

    @FXML
    private MenuItem listSC;

    @FXML
    private MenuItem collectionSC;

    @FXML
    private MenuItem refreshSC;

    @FXML
    private MenuItem quitSC;

    @FXML
    private ChoiceBox<String> collectionChoice;

    @FXML
    private Label labelNoteTitle;

    @FXML
    private Button selectAllButton;

    @FXML
    private Label labelNoNotes;

    @FXML
    private Button fileAddButton;

    @FXML
    private MenuItem enMenuItem;

    @FXML
    private MenuItem nlMenuItem;

    @FXML
    private MenuItem deMenuItem;

    @FXML
    private MenuItem friMenuItem;

    @FXML
    private MenuItem catMenuItem;

    @FXML
    private SplitMenuButton languageMenuButton;

    @FXML
    private Menu notesActionsMenu;

    @FXML
    private Menu filterMenu;

    @FXML
    private Menu otherShortcutsMenu;

    @FXML
    private MenuItem editContentSC;

    @FXML
    private MenuItem collectionDropdownSC;

    @FXML
    private Tooltip fileContextMenu;

    @FXML
    private MenuItem downloadFile;

    @FXML
    private MenuItem deleteFile;

    @FXML
    private MenuItem renameFile;

    @FXML
    private MenuItem insertReference;

    /**
     * The index of currently opened note
     * If the index is -1, it means that no note is selected
     */
    private int currentSelectedIndex = 0;

    // This boolean indicates
    // if the event listener should listen to note selection events
    private boolean selectionEventsEnabled = true;

    public void setEN() {
        setLanguage(Locale.ENGLISH);
    }

    public void setDE() {
        setLanguage(Locale.GERMAN);
    }

    public void setNL() {
        setLanguage(LOCALE_NL);
    }

    /** this method was created so the flag icon would also be set when initializing the application
     *
     * @param locale locale which was saved in the config
     */
    public void setLanguage(Locale locale) {
        languageService.setLocale(locale);
        configService.setLocale(locale);
        updateSelectAllButtonText();
        insertFlagsOnLabeled(languageMenuButton, localeToIcon.get(locale));
    }

    public  void setFrisian(){
        setLanguage(LOCALE_FRI);
    }

    public  void setCat(){
        setLanguage(LOCALE_CAT);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeLanguage();

        service.initializeNotes();
        md.initializeMarkdown(markdownWebView.getEngine());


        listViewCollectionsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        setupCollectionSelectionEvents();
        setupNoteSelectionEvents();

        // When focused on notes list, general keypress listener doesn't respond
        listViewNotesList.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE)
                textFieldNotesSearch.requestFocus();
        });
        listViewCollectionsList.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE)
                textFieldNotesSearch.requestFocus();
        });

        // Listens to changes in selected collection
        collectionChoice
                .getSelectionModel()
                .selectedIndexProperty()
                .addListener((_, _, _) -> {
                    if (!selectionEventsEnabled) {
                        return;
                    }
                    collectionConfigSelected();
                });
        listViewNotesList.setCellFactory(_ ->
                new TitleListCell(service, mc, languageService));
    }

    private void initializeLanguage() {
        bind();
        insertFlagsOnMenuItem(enMenuItem, BRITISH_FLAG);
        insertFlagsOnMenuItem(nlMenuItem, DUTCH_FLAG);
        insertFlagsOnMenuItem(deMenuItem, GERMANY_FLAG);
        insertFlagsOnMenuItem(friMenuItem, FRISIAN_FLAG);
        insertFlagsOnMenuItem(catMenuItem, CAT_FLAG);
        Locale configLocale = configService.getLocale();
        setLanguage(configLocale);
        languageService.setLocale(configLocale);
        updateSelectAllButtonText();
    }

    /**
     * The event listened is added to the listView of collections
     * to keep track of the ones that are currently selected
     */
    private void setupCollectionSelectionEvents() {
        listViewCollectionsList
                .getSelectionModel()
                .getSelectedIndices()
                .addListener((ListChangeListener<? super Integer>) change -> {
                    if (!selectionEventsEnabled) {
                        return;
                    }

                    boolean hasChange = false;
                    while (change.next()) {
                        hasChange = true;
                        for (int i : change.getAddedSubList()) {
                            selectCollectionsService.selectCollection(i);
                        }

                        for (int i : change.getRemoved()) {
                            selectCollectionsService.unselectCollection(i);
                        }
                    }

                    updateSelectAllButtonText();

                    if (hasChange) {
                        refreshNotesAndSelectNoteId();
                    }
                });
    }

    /**
     * The event listened is added to the listView of notes
     * to keep track of the currently selected note
     */
    private void setupNoteSelectionEvents() {
        listViewNotesList
                .getSelectionModel()
                .selectedIndexProperty()
                .addListener((_, _, newModel) -> {
                    if (!selectionEventsEnabled) {
                        return;
                    }
                    selectionEventsEnabled = false;
                    int newIndex = newModel.intValue();

                    // If something was selected by the user, the currentSelectedIndex is updated
                    if (service.isValidIndex(newIndex)) {
                        currentSelectedIndex = newIndex;
                        selectNote(currentSelectedIndex);
                    }
                    // If there are no notes, we can reset the selection
                    // Otherwise resetting the selection is not allowed
                    // Thus we ensure that a note is always selected
                    else if (listViewNotesList.getItems().isEmpty()){
                        currentSelectedIndex = -1;
                        resetSelection();
                    }
                    selectionEventsEnabled = true;
                });
    }

    private void bind() {
        languageService.bindLabeledTooltip(searchButton, "Item.search");
        languageService.bindLabeled(collectionButton, "Item.collections");
        languageService.bindLabeledTooltip(createBtn, "Item.add");
        languageService.bindLabeledTooltip(removeButton, "Item.remove");
        languageService.bindLabeledTooltip(refreshButton, "Item.refresh");
        languageService.bindLabeled(fileAddButton, "Item.fileAddButton");
        languageService.bindMenuItem(createSC, "Item.createSC");
        languageService.bindMenuItem(titleSC, "Item.titleSC");
        languageService.bindMenuItem(deleteSC, "Item.deleteSC");
        languageService.bindMenuItem(searchbarSC, "Item.selectSearchbarSC");
        languageService.bindMenuItem(searchSC, "Item.performSearchSC");
        languageService.bindMenuItem(listSC, "Item.moveToListSC");
        languageService.bindMenuItem(collectionSC, "Item.openCollectionPopupSC");
        languageService.bindMenuItem(refreshSC, "Item.refreshSC");
        languageService.bindMenuItem(quitSC, "Item.quitAppSC");
        languageService.bindTextInputControl(textAreaNoteContents, "Item.content");
        languageService.bindTextInputControl(textFieldNotesSearch, "Item.search");
        languageService.bindMenuItem(notesActionsMenu, "Item.noteActionsMenu");
        languageService.bindMenuItem(filterMenu, "Item.filterMenu");
        languageService.bindMenuItem(otherShortcutsMenu, "Item.otherShortcutsMenu");
        languageService.bindMenuItem(editContentSC, "Item.editContentSC");
        languageService.bindMenuItem(collectionDropdownSC, "Item.openCollectionDropdownSC");
        languageService.bindTooltip(fileContextMenu, "Item.fileContextMenu");
        languageService.bindMenuItem(downloadFile, "Item.menuItemDownloadFile");
        languageService.bindMenuItem(deleteFile, "Item.menuItemDeleteFile");
        languageService.bindMenuItem(renameFile, "Item.menuItemRenameFile");
        languageService.bindMenuItem(insertReference, "Item.menuItemInsertReference");
        languageService.bindLabeled(labelNoNotes, "Item.labelNoNotes");
    }

    /** This method sets a png on an menuItem
     *
     * @param menuItem the item where you want the image on
     * @param png the image
     */
    private void insertFlagsOnMenuItem(MenuItem menuItem, String png){
        ImageView imageView = createImageView(png);
        menuItem.setGraphic(imageView);
    }

    private void insertFlagsOnLabeled(Labeled labeled, String png){
        ImageView imageView = createImageView(png);
        labeled.setGraphic(imageView);
    }

    private ImageView createImageView(String png) {
        InputStream iconStream = getClass().getResourceAsStream(png != null ? png : QUESTION_MARK);
        if (iconStream == null) {
            throw new IllegalStateException(
                    languageService.getDescriptionByKey("Item.imageNotFound"));
        }
        Image image = new Image(iconStream);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(17);
        imageView.setFitHeight(17);
        return imageView;
    }

    /**
     * Keyboard shortcuts to navigate through the notes scene
     *
     * @param event - event
     */
    @FXML
    void handleKeyPress(KeyEvent event) {
        //check if title is being edited
        if (mc.getActiveCellCtrl() != null && mc.getActiveCellCtrl().isEditing()) {
            mc.getActiveCellCtrl().switchToReadMode();
        }

        // Searchbar actions
        if (event.getCode() == KeyCode.ESCAPE)
            focusOnSearch();
        else if (event.getCode() == KeyCode.ENTER && textFieldNotesSearch.isFocused())
            queryNotesClicked();

        else if (event.isAltDown()) {
            switch (event.getCode()) {
                // Show the shortcuts menu
                case A -> notesActionsMenu.show();

                // Show the searchbar shortcuts menu
                case F -> filterMenu.show();

                // Show the extra shortcuts menu
                case X -> otherShortcutsMenu.show();

                // Create a new note
                case N -> createNoteClicked();

                // Open collections popup
                case O -> editCollections();

                // Open collections dropdown
                case C -> focusOnCollectionsDropdown();

                // Move cursor to list
                case L -> focusOnList();

                // Delete current selected note
                case D -> deleteNoteClicked();

                // Title of note
                case T -> enterTitleEditingMode();

                // Edit content of note
                case E -> focusOnContent();

                // Refresh notes in current selected collection
                case R -> refreshApp();

                // Close app window
                case Q -> closeApp();
            }
        }
        event.consume();
    }

    public void updateSelectAllButtonText() {
        if (selectCollectionsService.areAllSelected()) {
            selectAllButton.setText(languageService.getDescriptionByKey("Item.selectDefault"));
        }
        else {
            selectAllButton.setText(languageService.getDescriptionByKey("Item.selectAll"));
        }
    }

    public void downloadClicked()  {
        try {
            fs.downloadFile(listViewFilesList.getSelectionModel().getSelectedIndex(),
                    configService.getCollectionConfig(
                            service.getCurrentNote().collection.collectionKey));
        } catch(Exception e) {
            throw new IllegalArgumentException(languageService.
                    getDescriptionByKey("Item.downloadError"));
        }
    }

    @FXML
    private void deleteFileClicked() {
        try {
            int selectedIndex = listViewFilesList.getSelectionModel().
                    getSelectedIndex(); // Get selected file index
            if (selectedIndex < 0) {
                new InformationalPopup().showInformation(languageService.
                                getDescriptionByKey("Item.errorDeletingFile"),
                        languageService.getDescriptionByKey("Item.noFileSelectedForDeleting"));
                return;
            }
            EmbeddedFileIdentification file = listViewFilesList
                    .getSelectionModel().getSelectedItem();

            fs.deleteFile(
                    selectedIndex,
                    configService.getCollectionConfig(
                            service.getCurrentNote().collection.collectionKey));
            String markdown = this.textAreaNoteContents.getText();
            String updatedMarkdown = fs.removeImageLinkFromMarkdown(markdown,
                    file.getFileKey().getFilename());
            this.textAreaNoteContents.setText(updatedMarkdown);

            List<EmbeddedFileIdentification> files = fs
                    .getAllFilesFromNote(service.getNoteInfoAtIndex(currentSelectedIndex).get());
            listViewFilesList.getItems().setAll(files);
            refreshNotesAndSelectNoteId();

            if (getSelectedNoteInfo().isPresent()) {
                NoteIdentification noteInfo = getSelectedNoteInfo().get();
                // update server
                service.updateNoteContent(selectedIndex, updatedMarkdown);
                refreshNotesAndSelectNoteInfo(noteInfo);
            }

            new InformationalPopup().showInformation(languageService.
                            getDescriptionByKey("Item.fileDeleted"),
                    languageService.getDescriptionByKey("Item.fileDeletionSucces"));
        } catch (Exception e) {
            refreshNotesAndSelectNoteId(); // refreshes the app, since the
            // selected file might not exist anymore, despite being shown
            new InformationalPopup().showInformation(languageService.
                            getDescriptionByKey("Item.errorDeletingFile"),
                    languageService.getDescriptionByKey("Item.failedToDeleteFile")
                            + e.getMessage());
        }
        hideOrRevealFiles();
    }

    public void renameFileClicked() {
        int selectedIndex = listViewFilesList.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) return; // No file selected
        EmbeddedFileIdentification file = listViewFilesList.getSelectionModel().getSelectedItem();
        // Show a dialog to enter a new name
        TextInputDialog dialog = new TextInputDialog(file.getFileKey().getFilename());
        dialog.setTitle(languageService.getDescriptionByKey("Item.renameFile"));
        dialog.setHeaderText(languageService.getDescriptionByKey("Item.enterANewNameForFile"));
        dialog.setContentText(languageService.getDescriptionByKey("Item.newName"));

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (newName.trim().isEmpty()) {
                throw new EmptyTitleException(
                        languageService.getDescriptionByKey("Item.titleOfFileCannotBeEmpty"));
            }
            try {
                fs.renameFile(
                        file.getFileKey().getNoteId(),
                        file.getFileKey().getFilename(),
                        newName.trim(),
                        configService.getCollectionConfig(
                                service.getCurrentNote().collection.collectionKey));
                // Update the Markdown content in the note
                String markdown = this.textAreaNoteContents.getText();
                String updatedMarkdown = fs.updateImageLinksForRenamedFile(markdown,
                        file.getFileKey().getFilename(), newName.trim());
                service.updateNoteContent(listViewNotesList.getSelectionModel().
                        getSelectedIndex(), updatedMarkdown);

                if (getSelectedNoteInfo().isPresent()) {
                    NoteIdentification noteInfo = getSelectedNoteInfo().get();
                    refreshNotesAndSelectNoteInfo(noteInfo);
                }
                List<EmbeddedFileIdentification> files = fs
                        .getAllFilesFromNote(service
                                .getNoteInfoAtIndex(currentSelectedIndex).get());
                // Find the renamed file in the refreshed list
                EmbeddedFileIdentification renamedFile = files.stream()
                        .filter(f -> f.getFileKey().getFilename().equals(newName.trim()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException(languageService.
                                getDescriptionByKey("Item.renamedFileNotFound")));
                files.remove(renamedFile);
                files.add(selectedIndex, renamedFile);
                // Update the ListView. Since the old file is deleted and replaced with a new
                // identical one, it should be kept at the same index as the old one
                listViewFilesList.getItems().setAll(files);
                listViewFilesList.getSelectionModel().select(selectedIndex);
                listViewFilesList.scrollTo(selectedIndex);
                new InformationalPopup().showInformation(languageService.
                                getDescriptionByKey("Item.renameSuccesful"),
                        languageService.getDescriptionByKey("Item.fileRenamedTo") + newName.trim());
            } catch (RuntimeException e) {
                new InformationalPopup().showInformation(languageService.
                        getDescriptionByKey("Item.errorRenaming") +
                        languageService.getDescriptionByKey("Item.File"), e.getMessage());
            }
        });
    }




    @FXML
    public void enterTitleEditingMode() {
        TitleListCellCtrl activeCtrl = mc.getActiveCellCtrl();

        if (activeCtrl == null) {
            int selectedIndex = listViewNotesList.getSelectionModel().getSelectedIndex();
            activeCtrl = mc.getCellController(selectedIndex);
            mc.setActiveCellCtrl(activeCtrl);
        }

        if (activeCtrl != null) {
            activeCtrl.switchToEditMode();
        }
    }

    public void focusOnSearch() { textFieldNotesSearch.requestFocus(); }
    public void focusOnList() { listViewNotesList.requestFocus(); }
    public void focusOnContent() { textAreaNoteContents.requestFocus(); }
    public void focusOnCollectionsDropdown() {
        collectionChoice.requestFocus();
        collectionChoice.show();
    }

    /**
     * Updates the listView to contain the list of titles of the notes
     * @param notes the list of notes
     */
    public void updateAllTitles(List<NoteIdentification> notes) {
        selectionEventsEnabled = false;
        listViewNotesList.getItems().clear();
        listViewNotesList.setItems(
                FXCollections.observableArrayList(notes));
        selectionEventsEnabled = true;
    }

    public void updateAllCollectionTitles(List<String> titles) {
        selectionEventsEnabled = false;
        listViewCollectionsList.getItems().clear();
        listViewCollectionsList.setItems(
                FXCollections.observableArrayList(titles));
        selectionEventsEnabled = true;
    }

    /**
     * Gets the title of collection of note at noteIndex
     * @param noteIndex the index of a note
     * @return the title of the collection
     */
    public Optional<String> getTitleOfNoteCollection(int noteIndex) {
        var note = service.getNoteInfoAtIndex(noteIndex);
        if (note.isEmpty()) {
            return Optional.empty();
        }

        return editCollectionsService
                .getTitleOfCollection(
                        note.get().collection.collectionKey);
    }

    /**
     * Shows all files in the list view
     * @param files to show
     */
    public void updateAllFiles(List<EmbeddedFileIdentification> files) {
        listViewFilesList.getItems().clear();
        listViewFilesList.setItems(
                FXCollections.observableArrayList(files));
    }

    public void updateAllCollectionsListView() {
        listViewCollectionsList.getSelectionModel().clearSelection();
        if (selectCollectionsService.getSelectedCollectionsIndexes().isEmpty()) {
            selectCollectionsService.resetSelection();
        }
        listViewCollectionsList.getSelectionModel().selectIndices(
                selectCollectionsService.getSelectedCollectionsIndexes().getFirst(),
                selectCollectionsService.getSelectedCollectionsIndexes()
                        .stream().mapToInt(Integer::intValue).toArray()
        );
    }

    public void refreshApp() {
        var optionalNoteInfo = service.getNoteInfoAtIndex(currentSelectedIndex);
        helperCtrl.setCollectionsCellFactory(listViewCollectionsList);

        editCollectionsService.removeAllInvalidConfigs();
        selectCollectionsService.keepOnlyValidSelectedCollections();
        try {
            selectCollectionsService.pullAllNotesInSelectedCollections();
        }
        catch (Exception e) {
            mc.showException(Thread.currentThread(), e);
            return;
        }

        updateAllCollectionTitles(
                configService
                        .getAllCollectionConfigs()
                        .stream()
                        .map(c -> c.title)
                        .toList());

        selectionEventsEnabled = false;
        updateAllCollectionsListView();
        selectionEventsEnabled = true;

        refreshNotesAndSelectNoteInfo(optionalNoteInfo.orElse(null));
    }

    /**
     * Refreshes the list of notes and tries to select the same note as before
     */
    public void refreshNotesAndSelectNoteId() {
        refreshNotesAndSelectNoteInfo(
                service.getNoteInfoAtIndex(currentSelectedIndex)
                        .orElse(null));
    }

    public void refreshNotesAndSelectNoteInfo(NoteIdentification noteInfoToSelect) {
        if (noteInfoToSelect == null)
            handleRefreshNotes(null);

        else {
            service.pushSelected();
            handleRefreshNotes(noteInfoToSelect);
        }
    }

    /**
     * Refreshes the list of notes
     * Loads the notes' info from the server and displays them
     * Selects the note at the given index
     * @param noteToSelect the note id be selected after the refresh (if such note exists)
     * If there is no such note, then the first note is selected
     */
    public void handleRefreshNotes(NoteIdentification noteToSelect) {
        editCollectionsService.removeAllInvalidConfigs();
        textFieldNotesSearch.setText(null);
        try {
            selectCollectionsService.pullAllNotesInSelectedCollections();
        }
        catch (Exception e) {
            mc.showException(Thread.currentThread(), e);
            return;
        }

        updateAllTitles(service.getListOfNotes());

        int newIndexToSelect = 0;
        if (noteToSelect != null) {
            var noteIndexId = service.getIndexOfNoteId(noteToSelect.id);
            var noteIndexTitle = service.getIndexOfNoteTitle(noteToSelect.title);

            newIndexToSelect = noteIndexId.orElse(noteIndexTitle.orElse(0));
        }

        selectNote(newIndexToSelect);
    }

    public void queryNotesClicked() {
        String query = textFieldNotesSearch.getText();
        try {
            selectCollectionsService.pullFilteredNoteInfosInSelectedCollections(query);
        }
        catch (Exception e) {
            mc.showException(Thread.currentThread(), e);
            return;
        }


        updateAllTitles(service.getListOfNotes());
        selectNote(0);
    }

    /**
     * Returns the currently selected note's info
     * If nothing is selected then return an empty optional
     *
     * @return currently selected note
     */
    private Optional<NoteIdentification> getSelectedNoteInfo() {
        if (!service.isValidIndex(currentSelectedIndex)) return Optional.empty();

        return service.getNoteInfoAtIndex(currentSelectedIndex);
    }

    /**
     * Displays the selected note for the user
     *
     * @param note the note to display
     */
    private void displayNote(Note note) {

        textAreaNoteContents.setText(note.content);
        labelNoNotes.setVisible(false);

        textAreaNoteContents.setDisable(false);
        fileAddButton.setVisible(true);
        markdownWebView.setVisible(true);
        removeButton.setVisible(true);
        hideOrRevealFiles();
        updateAllFiles(fs.getAllFilesFromNote(note));
        updateMarkdown(note.content);

        labelNoteTitle.setText(note.title);
        labelNoteTitle.setVisible(true);
        labelNoteTitle.setDisable(false);

        // The list of collection choice items is set here
        collectionChoice.setVisible(true);
        collectionChoice.setManaged(true);
        collectionChoice.setItems(FXCollections.observableArrayList(
                editCollectionsService.getListOfCollectionTitles()));

        // The index of the collection of currently selected note
        var index = configService.getIndexOfConfigKey(
                service.getCurrentNote().collection.collectionKey);

        selectionEventsEnabled = false;
        if (index.isPresent()) {
            collectionChoice.getSelectionModel().select(index.getAsInt());
        }
        else {
            collectionChoice.getSelectionModel().clearSelection();
        }
        selectionEventsEnabled = true;
        hideOrRevealFiles();

    }
    public void hideOrRevealFiles(){
        if(fs.getAllFilesFromNote(getSelectedNoteInfo().get()).isEmpty()){
            listViewFilesList.setVisible(false);
            AnchorPane.setBottomAnchor(fileAddButton, 5.0);
            AnchorPane.setBottomAnchor(markdownWebView, 35.0);
        } else{
            listViewFilesList.setVisible(true);
            AnchorPane.setBottomAnchor(fileAddButton, 85.0);
            AnchorPane.setBottomAnchor(markdownWebView, 115.0);
        }
    }

    /**
     * Resets the screen and disables the display
     */
    private void resetSelection() {
        labelNoteTitle.setText("");
        textAreaNoteContents.setText("");
        labelNoteTitle.setDisable(true);
        fileAddButton.setVisible(false);
        markdownWebView.setVisible(false);
        listViewFilesList.setVisible(false);
        removeButton.setVisible(false);

        textAreaNoteContents.setDisable(true);
        updateMarkdown("");
        listViewFilesList.getItems().clear();
        collectionChoice.getItems().clear();
        collectionChoice.setVisible(false);
        collectionChoice.setManaged(false);
        labelNoNotes.setVisible(true);
    }

    /**
     * Updates the UI of the listView to select a note
     *
     * @param noteIndex the index of the note to select
     */
    private void selectNoteInList(int noteIndex) {
        int listSize = listViewNotesList.getItems().size();
        if (noteIndex < 0 || noteIndex >= listSize)
            return;

        selectionEventsEnabled = false;
        currentSelectedIndex = noteIndex;
        listViewNotesList.getSelectionModel().select(noteIndex);
        selectionEventsEnabled = true;
    }

    /**
     * Selects a note in the list and displays its contents
     *
     * @param noteIndex the index of the note to select
     */
    public void selectNote(int noteIndex) {
        var optionalNote = service.pullNoteAtIndex(noteIndex);
        if (optionalNote.isEmpty()) {
            resetSelection();
            return;
        }

        selectNoteInList(noteIndex);
        displayNote(optionalNote.get());
        updateCurrentTitle(optionalNote.get());
    }

    /**
     * Assigns a note to the collection that is selected by the user
     */
    public void collectionConfigSelected() {
        int selectedIndex = collectionChoice.getSelectionModel().getSelectedIndex();
        if (!editCollectionsService.isValidIndex(selectedIndex))
            return;

        CollectionConfig config = editCollectionsService.getCollectionConfig(selectedIndex);
        service.updateNoteCollection(currentSelectedIndex, config);
        listViewCollectionsList.getSelectionModel().select(selectedIndex);
        refreshNotesAndSelectNoteId();
    }

    /**
     * Creates a note when the Add button is clicked
     */
    public void createNoteClicked() {
        if (configService.getDefaultCollectionKey() == null)
            throw new IllegalArgumentException(languageService.
                    getDescriptionByKey("Item.defaultNotSet"));

        mc.showCreateNotePopup();
    }

    public Note getServiceCurrentNote() { return service.getCurrentNote(); }

    /**
     * Deletes the note when the delete button is clicked
     */
    public void deleteNoteClicked() {
        textFieldNotesSearch.setText(null);
        listViewNotesList.requestFocus();
        var noteInfoToDelete = getSelectedNoteInfo();
        if (noteInfoToDelete.isEmpty()) return;

        // Show confirmation dialog
        boolean confirmed = showDeleteConfirmation(noteInfoToDelete.get().title);
        if (!confirmed) {
            return; // Do nothing if the user cancels
        }

        // Proceed with deletion
        service.deleteNote(noteInfoToDelete.get());

        // Refresh the UI
        handleRefreshNotes(null);
    }

    /**
     * Shows a confirmation dialog to the user before deleting a note.
     *
     * @param noteTitle the title of the note to delete
     * @return true if the user confirms, false otherwise
     */
    private boolean showDeleteConfirmation(String noteTitle) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(languageService.getDescriptionByKey("Item.noteDeletionTitle"));
        alert.setHeaderText(languageService.getDescriptionByKey("Item.noteDeletingHeader")
                + noteTitle);
        alert.setContentText(languageService.getDescriptionByKey("Item.noteDeletingContent"));

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public void updateCurrentTitle(NoteIdentification note) {
        listViewNotesList.getItems().set(currentSelectedIndex, note);
    }

    /**
     * Updates the note when content gets changed
     */
    public void updateNoteContentChanged() {
        if (getSelectedNoteInfo().isEmpty()) return;

        service.updateNote(currentSelectedIndex, labelNoteTitle.getText(),
                textAreaNoteContents.getText());
        updateMarkdown(textAreaNoteContents.getText());
    }

    /**
     * Updates the list view based on the current note's content
     */
    public void updateMarkdown(String inputContent) {

        //create temp file from content
        var noteInfo = getSelectedNoteInfo();
        if (noteInfo.isEmpty()) {
            md.createTemporaryHtmlFile("");
        }
        else {
            md.createTemporaryHtmlFile(
                    fs.replaceImageLinks(
                            inputContent,
                            getSelectedNoteInfo(),
                            configService.getCollectionConfig(
                                    noteInfo.get().collection.collectionKey)));
        }


        // Create the temporary path
        String path = Paths.get(System.getProperty("java.io.tmpdir"), "markdown.html").toString();
        markdownWebView.getEngine().load("file:///" + path.replace("\\", "/"));
    }

    public void checkThread() { service.checkThread(); }

    /**
     * Button action to open collection editing window
     */
    public void editCollections() {
        mc.showCollections();
    }

    public void selectAllButtonClick() {
        if (selectCollectionsService.areAllSelected()) {
            listViewCollectionsList.getSelectionModel().clearSelection();
            listViewCollectionsList.getSelectionModel().select(
                    editCollectionsService.getIndexOfDefaultConfigKey()
            );
        }
        else {
            listViewCollectionsList.getSelectionModel().selectAll();
        }
        listViewCollectionsList.requestFocus();
    }

    public void fileAddButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(languageService.getDescriptionByKey("Item.openFile"));
        File file = fileChooser.showOpenDialog(new Stage());
        textAreaNoteContents.setText((textAreaNoteContents.getText()
                + fs.addFile(file, getSelectedNoteInfo())));
        updateAllFiles(fs.getAllFilesFromNote(getSelectedNoteInfo().get()));
        updateNoteContentChanged();
        hideOrRevealFiles();
    }

    public void insertReferenceClicked() {
        int selectedIndex = listViewFilesList.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) return;
        EmbeddedFileIdentification file = listViewFilesList
                .getSelectionModel().getSelectedItem();
        if(file.contentType.equals("image/jpg")
                || file.contentType.equals("image/jpeg")
                || file.contentType.equals("image/png")
                || file.contentType.equals("image/gif")){
            textAreaNoteContents.setText(textAreaNoteContents.getText() +"\n!["+
                    file.getFileKey().getFilename()+"](" +
                    file.getFileKey().getFilename() + ")");
            updateNoteContentChanged();
            updateNoteContentChanged();
        } else{
            new InformationalPopup().showInformation(languageService.
                            getDescriptionByKey("Item.errorReferencingFile"),
                    languageService.getDescriptionByKey("Item.fileType"));
        }



    }

    public void closeApp() {
        Platform.exit();
    }
}