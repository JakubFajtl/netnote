package client.scenes;

import client.exceptions.EmptyTitleException;
import client.services.*;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CreateNoteCtrl implements Initializable {

    MainCtrl mc;
    NotesService ns;
    ConfigService cs;
    SelectCollectionsService scs;
    EditCollectionsService ecs;
    LanguageService ls;

    @Inject
    public CreateNoteCtrl(
            MainCtrl mc,
            NotesService ns,
            ConfigService cs,
            SelectCollectionsService scs,
            EditCollectionsService ecs,
            LanguageService ls) {

        this.mc = mc;
        this.ns = ns;
        this.cs = cs;
        this.scs = scs;
        this.ecs = ecs;
        this.ls = ls;
    }

    @FXML
    private TextField noteTitle;

    @FXML
    private Button createButton;

    @FXML
    private Label titleLabel;

    @FXML
    private Label collectionLabel;

    @FXML
    private ComboBox<String> collectionChoice;

    private int defaultIndexInDropdown;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ls.bindLabeled(createButton, "Item.create");
        ls.bindLabeled(titleLabel, "Item.noteTitle");
        ls.bindLabeled(collectionLabel, "Item.collectionForNote");
    }

    @FXML
    public void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) createClicked();

        else if (event.isAltDown() && event.getCode() == KeyCode.C)
            collectionChoice.requestFocus();
    }

    public void createClicked() {
        List<String> allCollectionTitles = ecs.getListOfCollectionTitles();
        int selectedCollectionIndex = allCollectionTitles.indexOf(collectionChoice.getValue());
        var config = cs.getCollectionConfig(selectedCollectionIndex);
        if (noteTitle.getText().trim().isEmpty())
            throw new EmptyTitleException(ls.getDescriptionByKey("Item.titleCannotBeEmpty"));
        ns.createNote(noteTitle.getText().trim(), config);
        scs.selectCollection(selectedCollectionIndex);
        Stage stage = ((Stage)collectionChoice.getScene().getWindow());
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public void setCollectionsAndFocusTitle() {
        List<String> allCollectionTitles = ecs.getListOfCollectionTitles();

        collectionChoice.getItems().clear();
        collectionChoice.getItems().addAll(allCollectionTitles);

        defaultIndexInDropdown = ecs.getIndexOfDefaultConfigKey();

        List<Integer> selectedIndexes = new ArrayList<>(scs.getSelectedCollectionsIndexes());
        if (selectedIndexes.size() == 1)
            collectionChoice.getSelectionModel().select(selectedIndexes.getFirst());

        else collectionChoice.getSelectionModel().select(defaultIndexInDropdown);

        setCollectionsCellFactory();

        noteTitle.clear();
        noteTitle.requestFocus();
    }

    private void setCollectionsCellFactory() {
        collectionChoice.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    if (getIndex() == defaultIndexInDropdown) {
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-font-weight: normal;");
                    }
                }
            }
        });
    }
}
