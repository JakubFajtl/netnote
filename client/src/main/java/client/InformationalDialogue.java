package client;

import client.services.LanguageService;
import jakarta.inject.Inject;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Region;

public class InformationalDialogue {
    private final LanguageService languageService;

    @Inject
    public InformationalDialogue(LanguageService languageService) {
        this.languageService = languageService;
    }

    public String showFirstCollectionServerUrlDialogue() {
        TextInputDialog dialog = new TextInputDialog("http://");
        dialog.setTitle(languageService.getDescriptionByKey("Item.pickDefaultServer"));
        dialog.setHeaderText(languageService.getDescriptionByKey("Item.enterServerURL"));
        dialog.setContentText(languageService.getDescriptionByKey("Item.currentlyNoCollection"));
        dialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        dialog.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        return dialog.showAndWait().orElse("");
    }
}
