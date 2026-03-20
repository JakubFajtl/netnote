package client.exceptions;

import client.services.LanguageService;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

import java.net.ConnectException;
import java.util.function.BiConsumer;

public class AlertExceptionHandler implements ExceptionHandler {

    private final LanguageService languageService;


    @Inject
    public AlertExceptionHandler(LanguageService languageService) {
        this.languageService = languageService;
    }


    /**
     * Gets the first cause of the exception
     * @param throwable the exception to get root cause from
     * @return the root cause of the exception
     */
    private Throwable getRootCause(Throwable throwable) {
        if (throwable.getCause() == null) {
            return throwable;
        }

        return getRootCause(throwable.getCause());
    }

    /**
     * Shows the exception alert to the user
     * @param throwable the exception that was thrown
     * @param nextAction the action to do after the user clicks "OK"
     */
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    public void showException(
            Thread thread,
            Throwable throwable,
            BiConsumer<ButtonType, Throwable> nextAction) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(languageService.getDescriptionByKey("Item.error"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
            Throwable innerException = getRootCause(throwable);

            if (innerException instanceof NotesApiException exception) {
                // This custom exception is thrown if a nice message can be shown to the user
                alert.setHeaderText(exception.getUserMessage());
            }
            else if (innerException instanceof TitleNotUniqueException) {
                alert.setHeaderText(languageService.
                        getDescriptionByKey("Item.duplicateTitleError"));
            }
            else if (innerException instanceof ConnectException ||
                    innerException instanceof ServerException) {
                // This exception means that there is no connection to the server
                printException(thread, throwable);
                alert.setHeaderText(languageService.
                        getDescriptionByKey("Item.serverNotResponding"));
                appendQuitAppButton(alert);
            }
            else if (innerException instanceof WebViewException) {
                alert.setHeaderText(languageService.getDescriptionByKey("Item.webviewError"));
            }
            else if (innerException instanceof DeleteDefaultException) {
                alert.setHeaderText(languageService.
                        getDescriptionByKey("Item.cannotDeleteDefaultCollection"));
            }
            else if (innerException instanceof EmptyTitleException) {
                alert.setHeaderText(languageService.getDescriptionByKey("Item.EmptyFile"));
            }
            else if (innerException instanceof ConfigException) {
                alert.setHeaderText(languageService.getDescriptionByKey("Item.configError"));
            }
            else {
                printException(thread, throwable);
                alert.setHeaderText(languageService.getDescriptionByKey("Item.unexpectedError"));
                appendQuitAppButton(alert);
            }
            alert.setContentText(innerException.getMessage());
            alert.showAndWait().ifPresent((buttonType) ->
                    nextAction.accept(buttonType, innerException));
        });
    }

    public void printException(Thread thread, Throwable throwable) {
        System.err.println("Exception in thread " + thread.getName());
        throwable.printStackTrace();
    }

    public void appendQuitAppButton(Alert alert) {
        alert.getButtonTypes().add(new ButtonType(
                languageService.getDescriptionByKey("Item.quitApplication"),
                ButtonBar.ButtonData.CANCEL_CLOSE));
    }
}
