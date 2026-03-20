package client.scenes;

import client.services.LanguageService;
import client.services.NotesService;
import commons.NoteIdentification;
import jakarta.inject.Inject;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;

import java.io.IOException;

public class TitleListCell extends ListCell<NoteIdentification> {

    private FXMLLoader fxmlLoader;
    private TitleListCellCtrl controller;
    private NotesService notesService;
    private MainCtrl mainCtrl;
    private LanguageService languageService;

    @Inject
    public TitleListCell(NotesService notesService, MainCtrl mainCtrl,
                         LanguageService languageService) {

        this.notesService = notesService;
        this.mainCtrl = mainCtrl;
        this.languageService = languageService;
    }

    public NotesService getNotesService() {
        return notesService;
    }

    @Override
    public void updateItem(NoteIdentification identification, boolean empty) {
        super.updateItem(identification, empty);

        if (empty || identification == null) {
            setStyle("");
            setText(null);
            setGraphic(null);

            if (controller != null) {
                controller.getCollectionLabel().setManaged(false);
                controller.getCollectionLabel().setVisible(false);
            }
            mainCtrl.registerCellController(getIndex(), null);
        } else {
            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(getClass()
                        .getResource("/client/scenes/TitleListCell.fxml"));
                try {
                    setGraphic(fxmlLoader.load());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                controller = fxmlLoader.getController();
                controller.setMainCtrl(mainCtrl);
                controller.setAndBindLanguageService(languageService);

                controller.setCell(this);
                controller.getSaveButton().setOnAction(_ -> {

                    String newTitle = controller.getEditedTitle();
                    getListView().getItems().set(getIndex(), identification);
                    notesService.updateNoteTitle(getIndex(), newTitle);
                    controller.switchToReadMode();
                    controller.setData(newTitle);
                });
            }

            controller.setCellStyle();
            mainCtrl.registerCellController(getIndex(), controller);
            controller.setData(identification.title);
            setGraphic(fxmlLoader.getRoot());

        }
    }
}
