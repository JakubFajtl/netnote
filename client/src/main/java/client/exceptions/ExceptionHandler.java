package client.exceptions;

import javafx.scene.control.ButtonType;

import java.util.function.BiConsumer;

public interface ExceptionHandler {
    void showException(
            Thread thread,
            Throwable throwable,
            BiConsumer<ButtonType, Throwable> nextAction
    );
}
