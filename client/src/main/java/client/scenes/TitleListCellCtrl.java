package client.scenes;

import client.services.LanguageService;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

import java.io.InputStream;

public class TitleListCellCtrl {

    @FXML
    private Label titleLabel;
    @FXML
    private Button editButton;
    @FXML
    private TextField titleTextField;
    @FXML
    private Button saveButton;
    @FXML
    private Label collectionLabel;

    private TitleListCell cell;

    private MainCtrl mainCtrl;

    private LanguageService languageService;

    @FXML
    public void initialize() {
        InputStream iconStream = getClass().getResourceAsStream("/images/pencil.png");
        if (iconStream == null) {
            throw new IllegalStateException(
                    languageService.getDescriptionByKey("Item.imageNotFound"));
        }
        Image image = new Image(iconStream);


        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(17);
        imageView.setFitHeight(17);

        editButton.setGraphic(imageView);

        titleTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                saveButton.fire();
            }
        });
    }

    public void setCell(TitleListCell cell) {
        this.cell = cell;

        editButton.setVisible(cell.isHover());
        cell.setOnMouseEntered(_ -> {
            if (!isEditing()) {
                editButton.setVisible(true);
            }
        });

        cell.setOnMouseExited(_ -> {
            if (!isEditing()) {
                editButton.setVisible(false);
            }
        });
    }

    /**
     * Sets the collection borders and the
     * collection label of the cell (if needed)
     */
    public void setCellStyle() {
        if (cell.getNotesService().isFirstCellInCollection(cell.getIndex())) {
            collectionLabel.setManaged(true);
            collectionLabel.setVisible(true);
            var collectionTitle =
                    mainCtrl.getNotesHomeCtrl()
                            .getTitleOfNoteCollection(cell.getIndex());
            collectionTitle.ifPresent(s -> collectionLabel.setText(s));

            if (cell.getNotesService().isLastCellInCollection(cell.getIndex())) {
                cell.setStyle("-fx-border-width: 2 2 2 2;");
            }
            else {
                cell.setStyle("-fx-border-width: 2 2 0 2;");
            }
            return;
        }

        collectionLabel.setManaged(false);
        collectionLabel.setVisible(false);
        if (cell.getNotesService().isLastCellInCollection(cell.getIndex())) {
            cell.setStyle("-fx-border-width: 0 2 2 2;");

        }
        else {
            cell.setStyle("-fx-border-width: 0 2 0 2;");
        }
    }

    public void setData(String title) {
        titleLabel.setText(title);
        titleTextField.setText(title);
        titleTextField.positionCaret(title.length());
    }

    public TextField getTitleTextField() {
        return titleTextField;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public Label getCollectionLabel() {
        return collectionLabel;
    }

    public void setMainCtrl(MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
    }

    public void setAndBindLanguageService(LanguageService languageService) {
        this.languageService = languageService;
        this.languageService.bindLabeledTooltip(saveButton, "Item.save");
    }

    public void switchToEditMode() {
        mainCtrl.getNotesHomeCtrl().selectNote(cell.getIndex());

        editButton.setVisible(false);
        titleLabel.setVisible(false);
        titleTextField.setVisible(true);
        saveButton.setVisible(true);

        titleTextField.requestFocus();

        if (mainCtrl != null) {
            mainCtrl.setActiveCellCtrl(this);
        }
    }

    public void switchToReadMode() {
        mainCtrl.setActiveCellCtrl(null);
        titleLabel.setVisible(true);
        editButton.setVisible(cell.isHover());
        titleTextField.setVisible(false);
        saveButton.setVisible(false);
        mainCtrl.getNotesHomeCtrl().refreshNotesAndSelectNoteId();
    }

    @FXML
    private void onEditClicked() {
        switchToEditMode();
    }

    @FXML
    private void onSaveClicked() {
        switchToReadMode();
    }

    public String getEditedTitle() {
        return titleTextField.getText().trim();
    }

    public boolean isEditing() {
        return titleTextField.isVisible();
    }

    /**
     * Method for checking if a mouse click was in a section e.g. text field or button
     * @param event - mouse click
     * @return - A boolean representing the outcome
     */
    public boolean isClickInsideSection(MouseEvent event, Node section) {
        if (!section.isVisible()) {
            return false;
        }
        Bounds bounds = section.localToScene(section.getBoundsInLocal());

        return bounds.contains(event.getSceneX(), event.getSceneY());
    }
}
