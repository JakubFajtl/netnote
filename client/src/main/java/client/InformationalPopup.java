package client;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.layout.Region;

public class InformationalPopup {

    public void showInformation(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        });
    }
}
