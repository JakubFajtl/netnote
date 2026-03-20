package client.scenes;

import client.services.EditCollectionsService;
import jakarta.inject.Inject;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class HelperCtrl {
    private final EditCollectionsService editCollectionsService;

    @Inject
    public HelperCtrl(EditCollectionsService editCollectionsService) {
        this.editCollectionsService = editCollectionsService;
    }

    public void setCollectionsCellFactory(ListView<String> listView) {
        var defaultIndex = editCollectionsService.getIndexOfDefaultConfigKey();

        listView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    if (getIndex() == defaultIndex) {
                        setStyle("-fx-font-weight: bold;");
                    }
                    else {
                        setStyle("-fx-font-weight: normal;");
                    }
                }
            }
        });
    }
}
