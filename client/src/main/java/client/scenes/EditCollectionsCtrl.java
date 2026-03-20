package client.scenes;

import client.InformationalPopup;
import client.enums.CollectionStatus;
import client.exceptions.ConfigException;
import client.exceptions.DeleteDefaultException;
import client.services.ConfigService;
import client.services.EditCollectionsService;
import client.services.LanguageService;
import com.google.inject.Inject;
import commons.CollectionConfig;
import commons.constants.AppConstants;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.ResourceBundle;

public class EditCollectionsCtrl implements Initializable{

    MainCtrl mc;
    EditCollectionsService ecs;
    LanguageService languageService;
    HelperCtrl helperCtrl;
    private final ConfigService configService;
    private int currentSelectedCollectionIndex;
    private boolean selectionEventsEnabled = true;

    @Inject
    public EditCollectionsCtrl(
            MainCtrl mc,
            EditCollectionsService ecs,
            LanguageService languageService,
            HelperCtrl helperCtrl, ConfigService configService) {
        this.mc = mc;
        this.ecs = ecs;
        this.languageService=languageService;
        this.helperCtrl = helperCtrl;
        this.configService = configService;
    }
    @FXML
    private ListView<String> listViewCollectionList;

    @FXML
    private TextField textFieldTitle;

    @FXML
    private TextField textFieldServer;

    @FXML
    private TextField textFieldCollection;

    @FXML
    private Label labelCurrentStatus;

    @FXML
    private Button buttonSaveCollection;

    @FXML
    private MenuItem titleCollectionSC;

    @FXML
    private MenuItem urlSC;

    @FXML
    private MenuItem collectionServerSC;

    @FXML
    private MenuItem addCreateCollectionSC;

    @FXML
    private MenuItem listCollectionSC;

    @FXML
    private MenuItem newCollectionSC;

    @FXML
    private MenuItem deleteCollectionSC;

    @FXML
    private Label titleLabel;

    @FXML
    private Label serverLabel;

    @FXML
    private Label collectionLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button buttonSetDefault;

    @FXML
    private Button createCollectionButton;

    @FXML
    private Button deleteCollectionButton;

    @FXML
    private Menu collectionActionMenu;

    @FXML
    private Menu otherShortcutsMenu;

    @FXML
    private MenuItem removeCollectionSC;

    @FXML
    private MenuItem setDefaultSC;

    @FXML
    private MenuItem closeWindowSC;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bind();
        // The event listened is added to the listView
        // to keep track of the currently selected note
        listViewCollectionList
                .getSelectionModel()
                .selectedIndexProperty()
                .addListener((_, _, newModel) -> {
                    if (!selectionEventsEnabled) {
                        return;
                    }
                    currentSelectedCollectionIndex = newModel.intValue();
                    displaySelectedCollection();
                });

        updateListViewCollectionList();
        selectCollectionInList(0);
        updateDefaultData();
    }

    private void bind(){
        languageService.bindMenuItem(titleCollectionSC, "Item.titleCollectionSC");
        languageService.bindMenuItem(urlSC, "Item.urlSC");
        languageService.bindMenuItem(collectionServerSC, "Item.collectionServerSC");
        languageService.bindMenuItem(addCreateCollectionSC, "Item.addCreateCollectionSC");
        languageService.bindMenuItem(listCollectionSC, "Item.listCollectionSC");
        languageService.bindMenuItem(newCollectionSC, "Item.newCollectionSC");
        languageService.bindMenuItem(deleteCollectionSC, "Item.deleteCollectionSC");
        languageService.bindLabeled(titleLabel, "Item.titleLabel");
        languageService.bindLabeled(serverLabel, "Item.serverLabel");
        languageService.bindLabeled(collectionLabel, "Item.collectionLabel");
        languageService.bindLabeled(statusLabel, "Item.statusLabel");
        languageService.bindLabeledTooltip(createCollectionButton, "Item.createCollection");
        languageService.bindLabeledTooltip(deleteCollectionButton, "Item.deleteCollection");
        languageService.bindMenuItem(collectionActionMenu, "Item.collectionActionMenu");
        languageService.bindMenuItem(otherShortcutsMenu, "Item.otherShortcutsMenu");
        languageService.bindMenuItem(removeCollectionSC, "Item.removeCollectionSC");
        languageService.bindMenuItem(setDefaultSC, "Item.setDefaultSC");
        languageService.bindMenuItem(closeWindowSC, "Item.closeWindowSC");
    }

    /**
     * Keyboard shortcuts to navigate through the collections scene
     *
     * @param event key combination
     */
    @FXML
    void handleKeyPress(KeyEvent event) {
        if (event.isAltDown()) {
            switch (event.getCode()) {
                //Open the menu with actions to edit current collection
                case E -> collectionActionMenu.show();

                // Focus on the extra shortcuts menu
                case X -> otherShortcutsMenu.show();

                // Title of collection
                case T -> focusOnTitle();

                // Url of server
                case U -> focusOnServer();

                // Collection at server
                case C -> focusOnCollection();

                // Update/create/add collection
                case A -> saveCollection();

                // Move to list
                case L -> focusOnList();

                // New collection
                case N -> clearTextFields();

                // Delete current collection
                case R -> removeCollection();

                // Set to default
                case D -> setCollectionAsDefault();

                // Close collections window
                case Q -> closeWindow();
            }
            event.consume();
        }
    }

    public void focusOnTitle() { textFieldTitle.requestFocus(); }
    public void focusOnServer() { textFieldServer.requestFocus(); }
    public void focusOnCollection() { textFieldCollection.requestFocus(); }
    public void focusOnList() { listViewCollectionList.requestFocus(); }
    public void closeWindow() {
        Stage stage = ((Stage)listViewCollectionList.getScene().getWindow());
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public String getServerUrl() {
        String serverUrl = textFieldServer.getText().trim();
        if (!serverUrl.endsWith("/")) {
            serverUrl += "/";
        }

        return serverUrl;
    }

    /**
     * Shows the label with current status to the user
     * Enables the create button (if needed)
     * @param status the status of a collection
     */
    public void setLabelToCurrentStatus(CollectionStatus status) {
        labelCurrentStatus.setText(languageService.getDescriptionByKey(status.getKey()));
        labelCurrentStatus.setTextFill(status.getColor());
        buttonSaveCollection.setVisible(status.isSuccess() && !status.isUpToDate());

        updateDefaultButton(status);

        switch (status) {
            case CollectionStatus.DOES_NOT_EXIST:
                buttonSaveCollection.setText(languageService.
                        getDescriptionByKey("Item.collectionCreate"));
                break;
            case CollectionStatus.EXISTS_REMOTELY:
                buttonSaveCollection.setText(languageService.getDescriptionByKey("Item.add"));
                break;
            case CollectionStatus.EXISTS_NOT_UP_TO_DATE:
                buttonSaveCollection.setText(languageService.
                        getDescriptionByKey("Item.collectionUpdate"));
                break;
        }
    }

    /**
     * Gets and updates the status of a collection
     * If the collection does not exist on the server, it is deleted
     */
    public void updateCollectionStatus() {
        String configKey = textFieldCollection.getText().trim();
        CollectionConfig config = ecs.createCollectionConfigLocally(
                configKey,
                textFieldTitle.getText().trim(),
                getServerUrl());

        CollectionStatus status = ecs.checkCollectionConfigStatus(config);

        if (status.equals(CollectionStatus.ERROR_DOES_NOT_EXIST) ||
                status.equals(CollectionStatus.ERROR)) {
            ecs.forceRemoveCollectionConfigLocally(config);
            throw new ConfigException(languageService.getDescriptionByKey(status.getKey()));
        }

        OptionalInt index = ecs.getIndexOfConfigKey(configKey);

        // If there is no collection with the current key, nothing is selected
        if (index.isEmpty()) {
            listViewCollectionList.getSelectionModel().clearSelection();
        }
        // Otherwise the collection with the current key is selected
        else {
            selectionEventsEnabled = false;
            selectCollectionInList(index.getAsInt());
            selectionEventsEnabled = true;
            currentSelectedCollectionIndex = index.getAsInt();
        }
        setLabelToCurrentStatus(status);
    }

    /**
     * Action for when the save button is pressed
     * Creates/renames a collection based on user input and changes the status text
     */
    public void saveCollection(){
        String configKey = textFieldCollection.getText().trim();
        CollectionStatus status =
                ecs.saveCollectionConfig(
                        ecs.createCollectionConfigLocally(
                                configKey,
                                textFieldTitle.getText().trim(),
                                getServerUrl()));
        setLabelToCurrentStatus(status);
        updateListViewCollectionList();
        var configIndex = ecs.getIndexOfConfigKey(configKey);
        if (configIndex.isPresent()) {
            selectCollectionInList(configIndex.getAsInt());
        }
        else {
            listViewCollectionList.getSelectionModel().clearSelection();
        }
    }
    /**
     * Updates the listview with the given list of strings
     */
    public void updateListViewCollectionList(){
        listViewCollectionList.getItems().clear();
        listViewCollectionList.setItems(
                FXCollections.observableArrayList(ecs.getListOfCollectionTitles()));
        updateDefaultData();
        selectCollectionInList(ecs.getIndexOfDefaultConfigKey());
    }

    /**
     * Updates the listView to mark the default collection in bold
     */
    public void updateDefaultData() {
        helperCtrl.setCollectionsCellFactory(listViewCollectionList);
    }

    /**
     * Updates the text of the "set default" button
     * @param status the status of the config
     */
    public void updateDefaultButton(CollectionStatus status) {
        if (status.canBeDefault()) {
            buttonSetDefault.setVisible(true);
            var defaultIndex = ecs.getIndexOfDefaultConfigKey();
            if (defaultIndex == currentSelectedCollectionIndex) {
                buttonSetDefault.setDisable(true);
                buttonSetDefault.setText(languageService.getDescriptionByKey("Item.default"));
            }
            else {
                buttonSetDefault.setDisable(false);
                buttonSetDefault.setText(languageService.getDescriptionByKey("Item.setAsDefault"));
            }
        }
        else {
            buttonSetDefault.setVisible(false);
        }
    }

    /**
     *  Copies the title and key of the selected note into the text fields displayed to the user
     */
    public void displaySelectedCollection(){
        if (!ecs.isValidIndex(currentSelectedCollectionIndex)){
            return;
        }
        textFieldTitle.setText(ecs.getCollectionConfig(
                currentSelectedCollectionIndex).title);
        textFieldCollection.setText(ecs.getCollectionConfig(
                currentSelectedCollectionIndex).collectionKey);
        textFieldServer.setText(ecs.getCollectionConfig(
                currentSelectedCollectionIndex).serverURL);
        textFieldTitle.setDisable(false);
        textFieldCollection.setDisable(false);
        textFieldServer.setDisable(false);
        updateCollectionStatus();
    }

    /**
     * Marks the current collection as default
     */
    public void setCollectionAsDefault() {
        ecs.setCollectionAsDefault(currentSelectedCollectionIndex);
        updateDefaultData();
        updateCollectionStatus();
        listViewCollectionList.requestFocus();

        new InformationalPopup().showInformation(
                languageService.getDescriptionByKey("Item.defaultCollectionUpdated"),
                languageService.getDescriptionByKey("Item.defaultCollectionExplanation"));
    }

    /**
     * removes selected collection and updates the list view
     */
    // the dialogue should have an option to remove it locally or remotely
    public void removeCollection(){
        if (!ecs.isValidIndex(currentSelectedCollectionIndex)){
            return;
        }

        CollectionConfig toRemove = ecs.getCollectionConfig(currentSelectedCollectionIndex);

        ecs.removeCollectionConfigLocally(currentSelectedCollectionIndex);
        updateListViewCollectionList();
        clearTextFields();

        new InformationalPopup().showInformation(
                languageService.getDescriptionByKey("Item.collectionWithTitle") + toRemove.title
                        + languageService.getDescriptionByKey("Item.hasBeenDeletedLocally"),
                languageService.getDescriptionByKey("Item.collectionStillExistAtUrl")
                        + toRemove.serverURL + "'"
                        + languageService.getDescriptionByKey("Item.underCollection")
                        + toRemove.collectionKey + "'");
    }

    /**
     * Selects the collection in the listView
     * @param toSelect the index of a collection to select
     */
    public void selectCollectionInList(int toSelect) {
        if (!ecs.isValidIndex(toSelect)) {
            clearTextFields();
            return;
        }

        listViewCollectionList.getSelectionModel().select(toSelect);
    }

    public void clearTextFields(){
        textFieldTitle.clear();
        textFieldServer.setText(AppConstants.DefaultServerUrl);
        textFieldCollection.clear();
        listViewCollectionList.getSelectionModel().clearSelection();
        updateCollectionStatus();
    }

    public void removeInvalidConfigs() {
        ecs.removeAllInvalidConfigs();
    }

    @FXML
    public void deleteCollectionClicked() {
        if (!ecs.isValidIndex(currentSelectedCollectionIndex)) {
            return;
        }

        CollectionConfig toRemove = ecs.getCollectionConfig(currentSelectedCollectionIndex);

        //Remove the collection from the local config
        ecs.removeCollectionConfigLocally(currentSelectedCollectionIndex);
        ecs.removeCollectionRemotely(toRemove);

        updateListViewCollectionList();

        new InformationalPopup().showInformation(
                languageService.getDescriptionByKey("Item.collectionDeleted"),
                languageService.getDescriptionByKey("Item.collection")
                        + toRemove.title + "' " +
                        languageService.getDescriptionByKey("Item.removedFromServerAndClient")
        );
    }

    public void popUpForDeleteCollection() {
        if (!ecs.isValidIndex(currentSelectedCollectionIndex)) {
            return;
        }
        CollectionConfig toRemove = ecs.getCollectionConfig(currentSelectedCollectionIndex);
        if (configService.getDefaultCollectionKey().equals(toRemove.collectionKey)) {
            throw new DeleteDefaultException(
                    languageService.getDescriptionByKey("Item.defaultCollectionCannotBeDeleted"));
        }

        // Create a confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(languageService.getDescriptionByKey("Item.deleteCollection"));
        alert.setHeaderText(languageService.getDescriptionByKey("Item.areYouSureDeleteCollection") +
                toRemove.title + "'?");
        alert.setContentText(languageService.getDescriptionByKey("Item.deleteLocallyOrServer"));

        ButtonType localOnlyButton = new ButtonType(
                languageService.getDescriptionByKey("Item.deleteLocally"));
        ButtonType serverButton = new ButtonType(
                languageService.getDescriptionByKey("Item.deleteFromServer"));
        ButtonType cancelButton = new ButtonType(
                languageService.getDescriptionByKey("Item.cancel"),
                ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(localOnlyButton, serverButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == localOnlyButton) {
                // Call removeCollection for local deletion
                removeCollection();
            } else if (result.get() == serverButton) {
                // Call deleteCollectionClicked for server  deletion
                deleteCollectionClicked();
            }
        }
    }

}
